This file contains the make file, the ReadMe file, and the code for my server as well as the code for my client. 
Below are some steps that can be done in order to run the project in different ways.
If you have an questions about it please let me know so I may clear it up.

Server
- To run the project you first need to go to the location of the server and run it
- it can be run without a debug 
- if you wish to use the debug use the command java Server.java DEBUG=1

Client
- to download: java Client.java localhost filename
- downloading a chunk: java Client.java localhost -s x -e y (x and y are the first and last byts)
- to upload to server: java Client.java localhost [-w] filename

localhost is the name of the server being used (eustis)