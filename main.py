import sys
import subprocess
import string
import time
import os
IS_PROD = False
IS_DEV = False
IS_TEST = False

def startDockerCompose():
    composeUpCmd = ["docker-compose", "up", "--build", "-d"]
    composeDownCmd = ["docker-compose", "down"]
    env = os.environ.copy()
    env["ENV"] = getEnvString()
    env["DATA_LOCATION"] = getDataLocation()
    subprocess.call(composeDownCmd, env=env,cwd='docker')
    subprocess.call(composeUpCmd, env=env,cwd='docker')

def run():
    if(not IS_PROD): goToSleep()
    
def contains(str,x): return str.find(x) >=0

def getEnvFromString(inputString):
    str = inputString.lower()
    if(str.find("pro") >= 0):
        return "production"
    if(str.find("te") >= 0):
        return "testing"
    return "development"

def goToSleep():
    print("sleeping")
    while True:
        time.sleep(10)

def getEnvString():
    if IS_PROD: return "production"
    if IS_DEV: return "dev"
    if IS_TEST: return "test"

def getDataLocation():
    if isWindows() and IS_PROD: return "C:\DATA"
    if IS_PROD: return "/data/"
    if IS_DEV: return ".dev_data"
    if IS_TEST: return ".test_data"
    
def isWindows(): os.name == 'nt'

def main():
    global IS_PROD
    global IS_DEV
    global IS_TEST
    ENV = getEnvFromString(sys.argv[1])
    cmd = sys.argv[2].lower()
    IS_PROD = contains(ENV, "pro")
    IS_DEV = contains(ENV, "dev")
    IS_TEST = contains(ENV, "test")
    print(ENV + " " + cmd)
    selectTask(cmd)

def selectTask(cmd):
    if(cmd == "run"): run()
    if(cmd == "compose"): startDockerCompose()

main()

