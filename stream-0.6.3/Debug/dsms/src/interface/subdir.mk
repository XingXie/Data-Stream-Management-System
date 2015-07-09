################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
O_SRCS += \
../dsms/src/interface/server.o 

CC_SRCS += \
../dsms/src/interface/server.cc 

OBJS += \
./dsms/src/interface/server.o 

CC_DEPS += \
./dsms/src/interface/server.d 


# Each subdirectory must supply rules for building sources it contributes
dsms/src/interface/%.o: ../dsms/src/interface/%.cc
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


