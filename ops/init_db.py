from threading import Thread
from time import sleep
import subprocess

keep_running = True

def init_db():
    thread = Thread(target=_init_db)
    thread.start()
    return thread

def _is_container_running():
    print("is container running? ...")
    return subprocess.call("docker top mysql") == 0

def _is_ready():
    print("is database ready? ...")
    return subprocess.call('docker exec mysql mysql -uroot -ppassword -e "select VERSION();"') == 0

def _wait_until_is_container_running():
    while keep_running and (not _is_container_running()): 
        sleep(1)
    if _is_container_running():
        return True
    else:
        print("container is not running")
        return False

def _wait_until_is_ready():
    while keep_running and (not _is_ready()): 
        sleep(1)
    if _is_ready():
        return True
    else:
        print("database is not ready")
        return False

def _send_init_script():
    print("initializing database")
    if( not subprocess.call('docker exec mysql bash -c "mysql -uroot -ppassword < /opt/mysql/init.sql"') == 0):
        print("initialization failed")
        return False
    else:
        return True

def _init_db():
    if not _wait_until_is_container_running(): return False
    if not _wait_until_is_ready(): return False
    _send_init_script()