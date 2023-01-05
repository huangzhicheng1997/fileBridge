FROM centos:7
ENV PROJECT_HOME "/home/fileBridge/"
ENV PROJECT_BIN "/home/fileBridge/bin"
RUN mkdir $PROJECT_HOME
RUN mkdir $PROJECT_BIN
ADD bin $PROJECT_BIN

ENV RUNDIR "/home/fileBridge/bin/native"
WORKDIR $RUNDIR
RUN echo "------------build  success---------"

#开发环境下手动进入到容器运行
#CMD ./fileBridge
