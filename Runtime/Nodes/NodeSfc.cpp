#include <iostream>
#include <vector>
#include <exception>

#include "Trace.h"
#include "ConfigReader.h"
#include "ServerEngine.h"

#include "arro.pb.h"
#include "NodeDb.h"
#include "NodeSfc.h"


using namespace std;
using namespace Arro;
using namespace arro;




class Interpreter {
private:

    std::list<Instruction>::iterator it;
    std::list<Instruction>::iterator end;

public:
    Interpreter(std::list<Instruction>& instr) {
        it = instr.begin();
        end = instr.end();

//        for(Instruction i: instr) {
//            switch(i.getState()) {
//                case CONDITION:
//                    // get value for this node
//                    // then collect all SINGLE_STATE values; if one of them match
//                    // then this condition is TRUE.
//                    break;
//                case IN:
//                    break;
//                case STATE_LIST_B:
//                    break;
//                case SINGLE_STATE:
//                    // check state of the node
//                    break;
//                case STATE_LIST_E:
//                    break;
//                case AND:
//                    break;
//                case OR:
//                    break;
//                case DONE:
//                    break;
//                default:
//                    break;
//            }
//        }

    }
    bool doCondition() {
        bool retval;

        std::string node = it->getArgument();

        ++it; // skip IN

        retval = doStateList(node);

        return retval;
    }
    bool doStateList(const std::string& node) {

        return false;

    }
};





NodeSfc::NodeSfc(Process* d, TiXmlElement* elt):
    m_trace("NodeSfc", true),
    m_process(d) {

    bool init = true;

    TiXmlElement* eltStep = elt->FirstChildElement("step");
    while(eltStep) {
        const string* nameAttr = eltStep->Attribute(string("name"));
        m_trace.println(std::string("New SfcStep ") + *nameAttr);
        m_steps.push_front(unique_ptr<SfcStep>(new SfcStep(*nameAttr, *this, init)));
        if(init) {
            m_currentSteps.insert(*nameAttr);
        }
        init = false; // TODO this is temporary

        eltStep = eltStep->NextSiblingElement("step");
    }


    TiXmlElement* eltTransition = elt->FirstChildElement("transition");
    while(eltTransition) {
        const string* conditionAttr = eltTransition->Attribute(string("condition"));
        m_trace.println(std::string("New SfcTransition with condition ") + *conditionAttr);
        m_transitions.push_front(std::unique_ptr<SfcTransition>(new SfcTransition(*conditionAttr, *this)));

//
//        TiXmlElement* eltAction = eltStep->FirstChildElement("entry-action");
//        while(eltAction) {
//            const string* actionNameAttr = eltStep->Attribute(string("node"));
//            const string* actionActionAttr = eltStep->Attribute(string("action"));
////            if(actionNameAttr && actionActionAttr) {
////                step->AddEntryAction(*actionNameAttr, *actionActionAttr);
////            }
//            eltAction = eltAction->NextSiblingElement("entry-action");
//        }
        eltTransition = eltTransition->NextSiblingElement("condition");
    }

}

void
NodeSfc::test() {
    m_trace.println("Testing expressions");
    for(auto it = m_transitions.begin(); it != m_transitions.end(); ++it) {
        (*it)->testRule0();
    }
}


void
NodeSfc::handleMessage(MessageBuf* m, const std::string& padName) {

// see NodePython for how to collect messages.


}

void
NodeSfc::runCycle() {

    //trace.println(string("NodeSfc input = ") + to_string((long double)actual_position));

    if(true /*actual_mode == "Active"*/) {
        //trace.println(string("NodeSfc output = ") + to_string((long double)output));
        for(auto it = m_transitions.begin(); it != m_transitions.end(); ++it) {
            (*it)->runTransitions(m_currentSteps);
        }
    }
}

bool
SfcTransition::testRule3(std::list<Instruction>::iterator& it, const std::string& argument) {
    ++it;
    bool ret = false;

    if(it!= m_instrList.end()) {
        // we now know that 'n' should be a step name
        ret = m_parent.hasStep(argument, it->getArgument());

        if(ret && it->match(SINGLE_STATE, STATE_LIST_E)) {
            ret = testRule4(it);
        } else {
            m_trace.println("Error in condition.");
        }
    }
    return ret;
}

