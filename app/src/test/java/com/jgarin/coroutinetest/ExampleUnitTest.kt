package com.jgarin.coroutinetest

import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Test

import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import kotlin.coroutines.CoroutineContext


class TestCoroutines {
    @get:Rule
    val rule = CoroutineSetMainRule()

    @Test
    fun `should fail`() {
        val param = "Hello World!"
        val repository: Repository = mockk()
        val b = ViewModel(repository)
        coEvery { repository.doStuff(param) } just runs
        b.doStuff(param)
        b.doStuff(param + "should fail")
        coVerify(exactly = 1) { repository.doStuff(param) }
    }

}

class CoroutineSetMainRule : TestRule {
    override fun apply(base: Statement?, description: Description?): Statement =
        object : Statement() {
            override fun evaluate() {

                val surrogate = TestCoroutineDispatcher()
                Dispatchers.setMain(surrogate)

                base?.evaluate()

                surrogate.cancel()
                Dispatchers.resetMain()
            }
        }
}

class TestCoroutineDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        block.run()
    }
}


class Repository {
    suspend fun doStuff(param: String) = withContext(Dispatchers.Default) {
        print(param)
    }
}

class ViewModel(private val repository: Repository) {
    fun doStuff(param: String) = GlobalScope.launch { // GlobalScope emulates `viewModelScope` here
        repository.doStuff(param)
    }
}
