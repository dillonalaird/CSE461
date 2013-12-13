To set up:

Step 1: Edit zombie.py to have controlIP set to the control server's IP
Step 2: Edit control.py to have the proper target IP address
Step 3: Run register.py on the control machine
Step 4: Start all the zombies running
Step 5: Run control.py to attack

1.) For our project we created a DoS tool as well as a framework for conducting DDoS attacks. For example you would use this tool to flood a server cutting off others from accessing it.


2/3.) Our model is based on a command/zombie model, where we have a command node who maintains lists of zombie nodes in the network, and issues commands to them.

There is a registration system, in which zombies send their ip addresses to the command server every so often (set to 60 seconds currently), and the command server stores them with a timestamp in the DB. When it is time to attack, the command server queries for zombies within a specific timerange (to get only active zombies), and sends a TCP message with space-separated tokens, indicating attack type, timeout, number of threads to run it on, etc.


We have three types of attacks.

UDP Flood: This attack works by sending 1024 byte packets through UDP to the server over and over. It sends it to whatever port, and doesn't wait for responses. It uses multiple threads to do this as well, using the bandwidth available as best as possible. It ends up flooding all the links on the way to the server, particularly when internal, and every computer that requires links on that route then can't connect to the internet properly. The UDP flood worked really well for us in killing the server.

HTTP GET Flood:
This is a pretty simple attack which will just spam our apache server with requests for an image. This one just puts a lot of load on the server and hogs all of it's resources since it has to continously serve lots of data. The target computer can still access the network, so the resource we are targeting here seems to be processing power.

slowloris attack:
For this attack, we are targeting an apache server. Apache2 is threadbased and by default has a max thread limit of about 100 threads. Our goal is to tie up all of these threads. To achieve this, we send HTTP requests very slowly by using a socket to send the request one character at a time and sleeping between sends. This allows us to hang onto a thread for a long time -- once all of the threads are used, the server can no longer serve to legitimate users.

4.) 

