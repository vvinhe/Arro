#ifndef ARRO_NODE_TIMER_H
#define ARRO_NODE_TIMER_H

#include "arro.pb.h"
#include "ConfigReader.h"
#include "Process.h"

namespace Arro
{
    class NodeTimer: public IDevice {
    public:
        /**
         * Constructor
         *
         * \param d The Process node instance.
         * \param name Name of this node.
         * \param params List of parameters passed to this node.
         */
        NodeTimer(Process* d, const std::string& name, ConfigReader::StringMap& params);
        virtual ~NodeTimer();

        // Copy and assignment is not supported.
        NodeTimer(const NodeTimer&) = delete;
        NodeTimer& operator=(const NodeTimer& other) = delete;

        /**
         * Handle a message that is sent to this node.
         *
         * \param msg Message sent to this node.
         * \param padName name of pad that message was sent to.
         */
        void handleMessage(MessageBuf* msg, const std::string& padName);

        /**
         * Make the node execute a processing cycle.
         */
        void runCycle();
    
        void timer ();
        static void init ();
        static void start ();
        static void stop ();

    private:
        Trace m_trace;
        int m_ticks;
        Process* m_device;
        std::string m_actual_mode;
    };
}

#endif
