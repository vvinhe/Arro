#ifndef ARRO_IDEVICE
#define ARRO_IDEVICE

#include <string>


namespace Arro {
    typedef std::string MessageBuf;

    /**
     * \brief Interface to be implemented by all devices.
     *
     * This interface is for implementing device instances:
     * - handleMessage will pass data to the device.
     * - runCycle will trigger running one execution cycle.
     */
    class IDevice {
    public:
        virtual ~IDevice() {};

        virtual void test() {};

        /**
         * Handle a message that is sent to this node.
         *
         * \param msg Message sent to this node.
         * \param padName name of pad that message was sent to.
         */
        virtual void handleMessage(MessageBuf* msg, const std::string& padName) = 0;

        /**
         * Make the node execute a processing cycle.
         */
        virtual void runCycle() = 0;
    };
}

#endif


