@echo off
java -jar jvmm-client.jar -m attach -a jvmm-agent.jar -s jvmm-server.jar -c config=config.yml %*