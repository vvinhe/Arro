// hack, see http://stackoverflow.com/questions/12523122/what-is-glibcxx-use-nanosleep-all-about
#define _GLIBCXX_USE_NANOSLEEP 1
#include <chrono>
#include <thread>
#include <algorithm>

#include "NodeDb.h"
#include "NodeTimer.h"

using namespace std;
using namespace Arro;
using namespace arro;

#define ARRO_TIMEOUT 1000

static list<NodeTimer*> timers;
static bool running = false;

NodeTimer::NodeTimer(Process* d, string& /*name*/, ConfigReader::StringMap& params):
    trace("NodePid", true),
    device(d) {

    try {
        ticks = stoi(params.at("ms"));
    }
    catch (std::out_of_range) {
        trace.println("### param not found ms ");
    }

    // Add this instance to array of timers.
    timers.push_back(this);
}

NodeTimer::~NodeTimer() {
    timers.remove(this);
}

void NodeTimer::handleMessage(MessageBuf* m, const std::string& padName) {
    if (padName == "mode") {
        Mode* msg = new Mode();
        msg->ParseFromString(m->c_str());

        assert(msg->GetTypeName() == "tutorial.Mode");
        actual_mode = ((Mode*)msg)->mode();
    }
}

void NodeTimer::runCycle() {
    // empty
}


void NodeTimer::timer () {
    Tick* tick = new Tick();

    tick->set_ms(ARRO_TIMEOUT /* elapsed time in ms */);

    try {
        device->getOutput("aTick")->submitMessage(tick);
    }
    catch(runtime_error&) {
        trace.println("Timer failed to update");
    }
}


static void refresh() {
    while(true)
    {
        if(running) {
            for_each(timers.begin(), timers.end(), [](NodeTimer* t) {t->timer(); });
        }
        std::chrono::milliseconds timespan(ARRO_TIMEOUT);
        std::this_thread::sleep_for(timespan);
    }
}


void NodeTimer::init () {
    // thread will run forever
    /* std::thread* first = */new std::thread(refresh);     // spawn new thread that calls refresh()
}

void NodeTimer::start() {
    running = true;
}
void NodeTimer::stop() {
    running = false;
}

