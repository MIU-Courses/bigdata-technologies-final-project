# Cloudera Quickstart Docker
- CDH 5.7
- JDK 1.8.0_401

## Build Docker Image

To build docker image run:
```
sh build_docker.sh
```

### Build issue on Windows

If the build is failed on Windows with WSL 2 that contains `exit code: 139` then do following steps to solve the build problem

- Create `%userprofile%\.wslconfig` file. The `%userprofile%` is the current user home directory.
- Add the following:
```
[wsl2]
kernelCommandLine = vsyscall=emulate
```
- Restart WSL by opening `Windows Powershell` as Administrator and run: 
```
wsl --shutdown
```
- Restart Docker Desktop
- Run build docker image command again