FROM cloudera/quickstart:latest

ARG DEBIAN_FRONTEND=noninteractive

ADD tools/jdk1.8.0_401 /usr/java/jdk1.8.0_401

RUN sudo yum -y remove java && \
    echo 'export JAVA_HOME=/usr/java/jdk1.8.0_401' >> /home/cloudera/.bashrc && \
    echo 'export PATH=$PATH:$JAVA_HOME/bin' >> /home/cloudera/.bashrc && \
    sudo update-alternatives --install /usr/bin/java java /usr/java/jdk1.8.0_401/bin/java 100 && \
    sudo update-alternatives --install /usr/bin/javac javac /usr/java/jdk1.8.0_401/bin/javac 100 && \
    source /home/cloudera/.bashrc && \
    echo 'export JAVA_HOME=/usr/java/jdk1.8.0_401' >> /etc/default/cloudera-scm-server && \
    echo 'export JAVA_HOME=/usr/java/jdk1.8.0_401' >> /etc/default/bigtop-utils && \
    echo 'export JAVA_HOME=/usr/java/jdk1.8.0_401' >> etc/profile