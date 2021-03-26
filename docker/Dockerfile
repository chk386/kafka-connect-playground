# FROM adoptopenjdk/openjdk8:jdk8u282-b08-alpine
FROM adoptopenjdk/openjdk11:jdk-11.0.10_9-alpine

LABEL maintainer="haekyu cho <haekyu.cho@nhn.com>"
LABEL title="alpine, adoptopenjdk, gradle, zsh, git, curl, vim -> base image"
LABEL version="1.0"

# app
RUN apk update && \
    apk add zsh git vim zsh-autosuggestions zsh-syntax-highlighting bind-tools curl sudo zsh-vcs && \
    rm -rf /var/cache/apk/*

# root password
# RUN echo 'root:r@@t' | chpasswd

# add user
ENV USER=docker
ENV HOME_DIR=/home
ENV UID=12345
ENV GID=23456
RUN addgroup --gid "$GID" "$USER" \
    && adduser \
    --disabled-password \
    --gecos "" \
    --home "$HOME_DIR" \
    --ingroup "$USER" \
    --uid "$UID" \
    "$USER" \
    && echo "$USER ALL=(ALL) NOPASSWD: ALL" > /etc/sudoers.d/$USER \
    && chmod 0440 /etc/sudoers.d/$USER

# gradle : https://services.gradle.org/distributions/
# ARG GRADLE=gradle-6.8.3
# RUN wget https://services.gradle.org/distributions/${GRADLE}-bin.zip -P /opt \
#     && mkdir -p /opt/gradle \
#     && unzip -d /opt/gradle /opt/${GRADLE}-bin.zip \
#     && rm -f *.zip
# ENV PATH="$PATH:/opt/gradle/${GRADLE}/bin"

USER $USER
WORKDIR $HOME_DIR

# oh my zsh
RUN sh -c "$(wget https://raw.github.com/robbyrussell/oh-my-zsh/master/tools/install.sh -O -)"

COPY "./zsh/.zshrc" $HOME_DIR
RUN sudo chmod 744 ${HOME_DIR}/.zshrc \
    && sudo chown docker ${HOME_DIR}/.zshrc \
    && sudo chgrp docker ${HOME_DIR}/.zshrc

# RUN echo "source /usr/shcdare/zsh/plugins/zsh-syntax-highlighting/zsh-syntax-highlighting.zsh" >> ~/.zshrc && \
#     echo "source /usr/share/zsh/plugins/zsh-autosuggestions/zsh-autosuggestions.zsh" >> ~/.zshrc


ENTRYPOINT ["/bin/zsh"]