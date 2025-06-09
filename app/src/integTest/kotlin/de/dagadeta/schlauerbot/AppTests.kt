package de.dagadeta.schlauerbot

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Answers.RETURNS_DEEP_STUBS
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
@AutoConfigureEmbeddedDatabase
class AppTests {

    @MockitoBean(answers = RETURNS_DEEP_STUBS)
    lateinit var api: JDA

    @BeforeEach
    fun prepareMocks() {
        val action: CommandListUpdateAction = mock()
        `when`(api.updateCommands().addCommands()).thenReturn(action)
    }

    @Test fun contextLoads() {}
}
