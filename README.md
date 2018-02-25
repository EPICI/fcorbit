# Orbit - Editor for Fantastic Contraption
[Play the game](http://www.fantasticcontraption.com/original/) | [Discord server](https://discord.gg/EYe5UDr)
---

The in-game editor lacked features.

sk89q's FCML editor, the first external editor, offered a lot more functionality, but still had limitations disguised as safety, and very annoyingly rounds all numbers during export, even angles.

ID36 then created an editor with much more flexibility, and doing more stuff clientside, but it lacked nice features like copy paste or undo redo.

Orbit, started in February 2018 by EPICI, aims to solve all those problems. Text editor and graphical editor side by side with real time updates and a large toolkit.

FAQ
===
Why can't I upload levels from within the editor?
---
Fantastic Contraption has numerous historical artifacts, and one of those is security by obscurity. It uses plain HTTP requests for a lot of things, with very little validation. It doesn't even force authentication, so you can do stuff under other people's accounts and access their supposedly private data. If you could upload from within the editor, the means would be in the code, and we can't allow that - so until further notice, no upload button.

What is the language used by the text editor?
---
Orbit uses a relaxed version of FCML (Fantastic Contraption Markup Language). FCML normally looks like this:
```
Type#index (center_x, center_y), (width, height), rotation_degrees, [joint_indices...]
BuildArea (-200, -100), (210, 210), 0
GoalArea (200, -50), (110, 110), 0
StaticRect (0, 20), (1000, 40), 0
StaticCircle (0, 20), (60, 60), 0
GoalRect#0 (0, -30), (30, 90), 90
GoalCircle#1 (0, -30), (40, 40), 0, [0]
```
Orbit's parser is very generous. This produces the same result:
```
Type#index center_x center_y width height rotation_degrees joint_indices...
BA -200 -100 210
GA 200 -50 110
SR 0 20 1000 40
SC 0 20 60
GR#0 0 -30 30 90 90
GC#1 0 -30 40 40 0 0
```
Leaving out the rotation makes it assumed to be 0. Leaving out the height makes it assumed to be equal to the width. For circles this is very convenient because rotation does nothing and width and height both need to be equal to the diameter anyway.

Type names have aliases so you don't need to worry too much about remembering the exact name, except for CW meaning clockwise and CCW meaning counterclockwise.

Commas, brackets, and other stuff that was previously necessary is now completely ignored by the parser. It can be considered syntactic salt.

Design pieces have indices and optionally joints. Indices count from 0 and are uniquely assigned. Joints say which other pieces a piece is connected to. The list is one way - you reference the lower indexed piece from the higher indexed piece, the game infers the opposite direction. In this example, piece #1, the goal circle, is connected to piece #0, the goal rectangle, and it is implied that it is the center that connects. For rods, if both sides are plausible, the left side of the higher indexed piece breaks the tie. Other nuances of jointing aren't really researched since it's really up to the game.
