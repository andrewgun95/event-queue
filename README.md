# Event Queue Implementation

A simple implementation of event queue handle both synchronous and asynchronous strategy.

## Classes

This implementation consists 2 abstract classes (or interfaces) supplied Event Queue.

  * Event Handler 
    - Handler class for receiving dispatched message from event queue
  * Event Message
    - Dispatched message class sent into event queue

### Asynchronous

  * Asynchronous Event Dispatch Processor
    - Process a dispatched message to all available event handlers.
    - The processor will distribute the message into each chunks of event handlers 
    (each chunk will be processed on individual thread).
    - For example : 100 event handlers and 40 unit of event handlers (chunks) will be processed on 3 tasks of threads.
  * Event Processor Task
    - Submit a task to a thread which only can be submitted 1 task per thread