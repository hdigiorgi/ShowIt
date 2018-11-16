#!/bin/bash
set -e

# Start the MySQL daemon in the background.

/usr/sbin/mysqld --datadir=/var/lib/mysql --default-authentication-plugin=mysql_native_password &
mysql_pid=$!

until mysqladmin ping >/dev/null 2>&1; do
  echo -n "."; sleep 0.2
done

cd /opt/mysql
#mysql < init.sql

wait $mysql_pid