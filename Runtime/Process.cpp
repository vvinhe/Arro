#include <iostream>
#include <vector>
#include <exception>

#include "Trace.h"
#include "ServerEngine.h"
#include "NodeSfc.h"
#include "NodePid.h"
#include "NodePython.h"
#include "NodePass.h"
#include "NodeTimer.h"
#include "NodeServo.h"
#include "ConfigReader.h"
#include "NodeDb.h"
#include "Process.h"


using namespace Arro;
using namespace std;


Process::Process(NodeDb& db, const string& instance):
    AbstractNode{instance},
    trace{"Process", true},
    nodeDb{db},
    device{nullptr},
    doRunCycle{false} {

    trace.println("Creating sfc " + instance + "._sfc");

    trace.println("---> Never used?? <--- ");

    db.registerNode(this, instance + "._sfc");

}

Process::Process(NodeDb& db, const string& url, const string& instance, ConfigReader::StringMap params, TiXmlElement* elt):
    AbstractNode{instance},
    trace{"Process", true},
    nodeDb{db},
    m_enableRunCycle{false},
    device{nullptr},
    doRunCycle{false} {

    trace.println("Creating instance of " + url);


    getPrimitive(url, instance, params, elt);

    std::map<std::string, std::string>::iterator iter;

    for (iter = params.begin(); iter != params.end(); ++iter) {
        trace.println("    parameter " + iter->first + " " + iter->second);

        auto kv = new arro::KeyValuePair();
        kv->set_key(iter->first.c_str());
        kv->set_value(iter->second.c_str());
        string s = kv->SerializeAsString();
        MessageBuf msg(s);
        free(kv);
        device->handleMessage(&msg, "config");
    }

    db.registerNode(this, instance);

}

Process::~Process() {
    if(device) delete device;
}

void
Process::runCycle() {
    if(doRunCycle) {
        device->runCycle();
        doRunCycle = false;
    }
}

void
Process::registerInput(const string& interfaceName, bool enableRunCycle) {
    m_interfaceName = interfaceName;
    m_enableRunCycle = enableRunCycle;
    nodeDb.registerNodeInput(this, interfaceName, [this](MessageBuf* msg) {
        if(m_enableRunCycle) {
            doRunCycle = true;
        }
        device->handleMessage(msg, m_interfaceName);
    });
}

void
Process::registerOutput(const string& interfaceName) {
    nodeDb.registerNodeOutput(this, interfaceName);
}

NodeDb::NodeSingleInput*
Process::getInput(const string& name) const {
    auto in = nodeDb.getInput(getName() + "." + name);
    if(in) {
        return in;
    } else {
        trace.println("no such input registered: " + getName() + "." + name);
        throw std::runtime_error("No such input registered: " + getName() + "." + name);
    }
}

NodeDb::NodeMultiOutput*
Process::getOutput(const string& name) const {
    auto out = nodeDb.getOutput(getName() + "." + name);
    if(out) {
        return out;
    } else {
        trace.println("no such output registered: " + getName() + "." + name);
        throw std::runtime_error("No such output registered: " + getName() + "." + name);
    }
}

void
Process::getPrimitive(const string& url, const string& instance, ConfigReader::StringMap& params, TiXmlElement* elt) {
    device = nullptr;

    if(url.find("Python:") == 0) {
        trace.println("new NodePython(" + instance + ")");
        try {
            string className = url.substr(7);
            device = new NodePython(this, className, params);
        } catch(out_of_range &) {
            throw std::runtime_error("Invalid URL for Python node " + url);
        }
    } else if(url.find("Sfc:") == 0) {
        trace.println("new NodeSfc(" + instance + ")");
        try {
            //string className = url.substr(7);
            device = new NodeSfc(this, elt);
        } catch(out_of_range &) {
            throw std::runtime_error("Invalid URL for SFC node " + url);
        }
    }
    else if(url.find("Native:") == 0) {
        try {
            string className = url.substr(7);

            if(className == "pid") {
                trace.println("new NodePid(" + instance + ")");
                device = new NodePid(this, instance, params);
            }
            else if(className == "Servo") {
               trace.println("new NodeServo(" + instance + ")");
                new NodeServo(this, instance, params);
            }
//            else if(className == "Linear") {
//                trace.println("new NodeLinear(" + instance + ")");
//                device = new NodeLinear(this, instance, params);
//            }
//            else if(className == "TsReader") {
//                trace.println("new NodeTsReader(" + instance + ")");
//                //device = new NodeTsReader(instance, params);
//            }
//            else if(className == "TsSection") {
//                trace.println("new NodeTsSection(" + instance + ")");
//                //device = new NodeTsSection(instance, params);
//            }
            else if(className == "Timer") {
                trace.println("new NodeTimer(" + instance + ")");
                device = new NodeTimer(this, instance, params);
            }
            else if(className == "pass") {
            }
            else {
                trace.println("unknown node" + instance );
                ServerEngine::console(string("unknown node ") + className);
            }
        } catch(out_of_range &) {
            trace.println("native node not found");
        }
    }
    if(device == nullptr) {
        trace.println("Node module not found " + url);
        throw std::runtime_error("Node module not found " + url);
    }
}

/**
 * TODO this is not the happiest function, it is for SFC only. Should be something more elegant.
 * @param sfc
 */
void
Process::registerSfc(const std::string& name, Process* sfc) {
    ((NodeSfc*)device)->registerSfc(name, (NodeSfc*)(sfc->device));
}



