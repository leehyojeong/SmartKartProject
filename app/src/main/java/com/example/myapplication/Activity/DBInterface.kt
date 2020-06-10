package com.example.myapplication.Activity

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction


public interface DBInterface {
    /**
     * Invoke the Lambda function "AndroidBackendLambdaFunction".
     * The function name is the method name.
     */
    @LambdaFunction
    fun androidDBLambda(request: DBRequestClass?): DBResponseClass?
}