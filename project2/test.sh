#!/bin/bash

# This tests to make sure the server can handel multiple threads simultaneously
java Client &
java Client &
java Client &
java Client &
java Client &

