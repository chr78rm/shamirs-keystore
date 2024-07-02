@echo off
java -version
set SHAMIRS_VERSION=1.4.0
java -Dde.christofreichardt.jca.shamirsdemo.console.echo=ON -jar target/shamirs-demo-%SHAMIRS_VERSION%.jar
