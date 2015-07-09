################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
O_SRCS += \
../net_server/src/command_conn.o \
../net_server/src/gen_conn.o \
../net_server/src/in_conn.o \
../net_server/src/net_mgr.o \
../net_server/src/net_util.o \
../net_server/src/out_conn.o \
../net_server/src/queue.o \
../net_server/src/thread.o 

CC_SRCS += \
../net_server/src/command_conn.cc \
../net_server/src/gen_conn.cc \
../net_server/src/in_conn.cc \
../net_server/src/net_mgr.cc \
../net_server/src/net_util.cc \
../net_server/src/out_conn.cc \
../net_server/src/queue.cc \
../net_server/src/thread.cc 

OBJS += \
./net_server/src/command_conn.o \
./net_server/src/gen_conn.o \
./net_server/src/in_conn.o \
./net_server/src/net_mgr.o \
./net_server/src/net_util.o \
./net_server/src/out_conn.o \
./net_server/src/queue.o \
./net_server/src/thread.o 

CC_DEPS += \
./net_server/src/command_conn.d \
./net_server/src/gen_conn.d \
./net_server/src/in_conn.d \
./net_server/src/net_mgr.d \
./net_server/src/net_util.d \
./net_server/src/out_conn.d \
./net_server/src/queue.d \
./net_server/src/thread.d 


# Each subdirectory must supply rules for building sources it contributes
net_server/src/%.o: ../net_server/src/%.cc
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


