![alt text](./banner.png "banner")

# Project TrainControl
To implement an autonomous DCC model railway control system with Arduino DCC++ command station and JMRI API.

## Features

### Acceleration / Deceleration
Emulates the acceleration and deceleration of the train.

### Distance Control
Controls the train to move for a certain distance, based on the preset speed mapping.

### Layout Construction and Path Finding
Decodes the JSON-formatted layout file to construct the layout in code and integrates path finding algorithm into it.

### Block Section Operation
Enables computed [block section](https://en.wikipedia.org/wiki/Absolute_block_signalling) operation which prevents train collision.

### Turnout Control
Controls the turnout from code. Integrates with block section operation for automated control.

### Sensor Support
Calibrates moving distance by reacting to IR sensor events. Improves distance control accuracy under block section operation.

## WIP Features

### Fully Autonomous Operation
To have all the trains in the system operate autonomously.

### Support for crossovers
To add support for crossovers and bi-direction operation.
