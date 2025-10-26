#!/bin/bash
docker stop team8-backend_container
docker container rm team8-backend_container
docker image rm team8-backend
