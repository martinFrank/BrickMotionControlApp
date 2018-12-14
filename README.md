# Motion Control App (java)

let's control our LEGO (tm) bricks with the Raspberry Pi
controller! This projekt provides an app that lets you easily
control your LEGO (tm) construction.

NOTE: there is an Motion Control Server here that grants you an
easy-to-use REST-API which the app uses.

## Big Picture

![Image of Mobile Icon](docs/BigPicture.png)

The `Brick Motion Control App` takes usage of the REST API
thus sending steering commands to the server. These steering
signals are processed by the Raspi and the corresponding Pins
are set.

## Setup App
you have to clone / download the project and build it. This
project uses gradle build.

if you use an IDE (like Eclipse or Intellij) they provide upload
via IDE. If you don't have one of theese you'll have to create
he apk with gradle build. You can find the apk file under
`\app\build\outputs\apk\release`

## App usage

![Image of Mobile Icon](docs/AppUI.png)

### Server config
it's quite simply, enter the proper IP into the server textfield.
this screenshot has currently `Name` as server IP written, you
should find something better (`192.168.0.11` e.g.)

**Note:** the server will be used to create the url for the REST-calls, as
for this example it would be `http://Name:8080/motion?pwma=33&pwmb=-23`
but if you had set it up properly it would be something like
`http://192.168.0.11:8080/motion?pwma=33&pwmb=-23` The query parameters
(33,-23) do come from the current motion you set via the touch pad
(the circle), see below.

### Console
Under the Server configuration there is a small text field, it is used
as console to print the current events that happened.


### Touch pad
The round field in the lower center is the touch pad. the further
you go from the center the more power you send to the server. This
layout is designed for a caterpillar like drive (as the server is set
to caterpillar as well). You will get an optical Feedback about the
percentage of power you send to the motion control server. The progress
bars to the left and right show you the amout, from -100% up to 100% -
if you don't want to move, the progress bar will be in the middle. that
is that they don't have any power forward as well as no power backward.

**Note:** if you move your finger to the top, both motors will be send
100% forward, if you move to the very left, left motor will be 100%
forward but right motor will be 100% backward. This will result into a
rotation without any formward movement.


## Features (planned)

- [ ] retrieve configuration from the server (caterpillar or wheel-based)

- [ ] configure more actors (lights, flashing lights, more motors)

- [ ] retrieve video signal from raspi as well

