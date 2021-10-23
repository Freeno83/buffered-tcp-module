# buffered-tcp-driver
Places received TCP messages into a buffer to help prevent missed messages.  This module was written after noticing that the stock Ignition provided TCP driver was missing counts due to the fact that multiple messages arrived to the port at once.  This happens when multiple packages are diverted at the same or nearly the same time.

## Usage

The module to install is located in: 

```
buffered-tcp-device-build\target\Buffered-TCP-Driver-signed.modl
```
### Creating an Instance
After installing the module in the target gateway, the following driver option will become available:

<p align="left">
  <img src="https://gecgithub01.walmart.com/scils-ignition/buffered-tcp-driver/blob/master/deviceType.JPG" width="350" title="Device Type">
</p>

The settings page looks like this.  The key settings are the IP address and port of the device which provides the data (the server).

<p align="left">
  <img src="https://gecgithub01.walmart.com/scils-ignition/buffered-tcp-driver/blob/master/deviceSettings.JPG" width="600" title="Device Settings">
</p>

### OPC-UA Address Syntax
The syntax to access the message is the same as the Ignition TCP driver:

```
ns=1;s=[Device name]port/Message
```

<p align="left">
  <img src="https://gecgithub01.walmart.com/scils-ignition/buffered-tcp-driver/blob/master/messageSyntax.JPG" width="800" title="Message Access Syntax">
</p>

### Using the data
This module is designed to be driven at a 1,000ms (1 second) scan rate.  Because the messages are buffered, there is no need to drive it faster.

Values in the message OPC-UA tag are a comma seperated string.  Then in order to access each individual message, that string has to be split on **comma** like follows:

```
messageList = str(tag.value).split(",")
```

### Checking the Status
The device status will be visible in the gateway just like any other device including Ignitions own TCP driver.

<p align="left">
  <img src="https://gecgithub01.walmart.com/scils-ignition/buffered-tcp-driver/blob/master/deviceStatus.JPG" width="800" title="Device Status">
</p>

In the system gateway tags in the designer, the enabled and connection status values are available.

<p align="left">
  <img src="https://gecgithub01.walmart.com/scils-ignition/buffered-tcp-driver/blob/master/designerStatus.JPG" width="300" title="Designer Status">
</p
