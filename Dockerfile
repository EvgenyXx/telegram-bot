FROM ubuntu:latest
LABEL authors="evgeny"

ENTRYPOINT ["top", "-b"]