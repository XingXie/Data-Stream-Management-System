################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
O_SRCS += \
../dsms/src/execution/indexes/hash_index.o 

CC_SRCS += \
../dsms/src/execution/indexes/hash_index.cc 

OBJS += \
./dsms/src/execution/indexes/hash_index.o 

CC_DEPS += \
./dsms/src/execution/indexes/hash_index.d 


# Each subdirectory must supply rules for building sources it contributes
dsms/src/execution/indexes/%.o: ../dsms/src/execution/indexes/%.cc
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


