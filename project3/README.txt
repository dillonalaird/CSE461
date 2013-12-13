To set up:

Step 1: Edit zombie.py to have controlIP set to the control server's IP
Step 2: Edit control.py to have the proper target IP address
Step 3: Run register.py on the control machine
Step 4: Start all the zombies running
Step 5: Run control.py to attack

1.) For our project we created a DoS tool as well as a framework for conducting DDoS attacks. For example you would use this tool to flood a server cutting off others from accessing it.


3.) Our model is based on a command/zombie model, where we have a command node who maintains lists of zombie nodes in the network, and issues commands to them.

There is a registration system, in which zombies send their ip addresses to the command server every so often (set to 60 seconds currently), and the command server stores them with a timestamp in the DB. When it is time to attack, the command server queries for zombies within a specific timerange (to get only active zombies), and sends a TCP message with space-separated tokens, indicating attack type, timeout, number of threads to run it on, etc.

4.) 
