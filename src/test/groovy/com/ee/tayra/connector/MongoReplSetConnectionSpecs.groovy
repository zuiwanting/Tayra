package com.ee.tayra.connector

import spock.lang.*;

import com.ee.tayra.connector.MongoReplSetConnection;
import com.mongodb.MongoClient
import com.mongodb.MongoException
import com.mongodb.ServerAddress

public class MongoReplSetConnectionSpecs extends Specification {

	private String source
	private int port
	private MongoReplSetConnection mongoReplsetConnection

	def setup() {
		source = 'localhost'
		port = 27017
		mongoReplsetConnection = new MongoReplSetConnection(source, port)
	}

	def cleanup() {
		mongoReplsetConnection = null
	}

	def loansTheMongoConnectionOnceAvailable() {
		given: 'the closure which needs the mongo connection'
			def actual = null
			def execute = { mongo -> actual = mongo }

		when: 'using the connection'
			mongoReplsetConnection.using(execute)

		then: 'ensure mongo connection is available'
			actual instanceof MongoClient
	}

	def allowsUserOperationBetweenNodeCrashAndReelectionAttempt() {
		given: 'the node crashes'
			def called = false
			def execute = {
				if(!called) {
					throw new MongoException.Network('The Node Crashed', new IOException())
				}
			}

		and: 'a retry closure'
			def retry = { called = true }

		when: 'using the connection'
			mongoReplsetConnection.using(execute, retry)

		then: 'ensure retry was invoked'
			called
	}

	def doesNotReactToAnyFailureOtherThanNodeCrash() {
		given: 'a problem other than node crash occurs'
			def notCalled = true
			def execute = {
				if(notCalled) {
					throw new MongoException('Non Node-Crash Exception')
				}
			}

		and: 'a retry closure'
			def retry = { notCalled = false }

		when: 'using the connection'
			mongoReplsetConnection.using(execute, retry)

		then: 'ensure retry was not invoked'
			notCalled
			thrown(MongoException)
	}

	def doesNotSurviveNodeCrashWhenRetryableIsFalse() {
		given: 'Mongo replica set connection with retryable as false'
			mongoReplsetConnection = new MongoReplSetConnection(source, port, false)
			and: 'node crashes'
			def called = false
			def execute = {
				if(!called) {
					throw new MongoException.Network('Node Crashed', new IOException())
				}
			}
		and: 'a retry closure'
			def retry = { called = true }

		when: 'using the connection'
			mongoReplsetConnection.using(execute, retry)

		then: 'ensure retry was not invoked'
			!called
	}
}