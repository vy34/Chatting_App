package com.example.chatting_app

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VoiceToTextParser(
    private val app: Application,
    private val vm: LCViewModel
):RecognitionListener {
    val voice=vm.voice.value

    private val _state= MutableStateFlow(voice)
    val state=_state.asStateFlow()
//    val recognizer=SpeechRecognizer.createSpeechRecognizer(app)
    private var recognizer: SpeechRecognizer? = null

    init {
        if (SpeechRecognizer.isRecognitionAvailable(app)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(app).apply {
                setRecognitionListener(this@VoiceToTextParser)
            }
        } else {
            _state.update {
                it?.copy(
                    error = "Speech recognition is not available on this device"
                )
            }
        }
    }

    fun startListening(languageCode:String){
        _state.update { voice }
        if (recognizer == null) {
            _state.update {
                it?.copy(
                    error = "Recognizer not available"
                )
            }
            return
        }
        val intent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE,languageCode)
        }
//        recognizer.setRecognitionListener(this)
//        recognizer.startListening(intent)
        recognizer?.startListening(intent)


        _state.update {
            it?.copy(
                isSpeaking = true
            )
        }
    }

    fun stopListening(){
        recognizer?.stopListening()
        _state.update {
            it?.copy(
                isSpeaking = false
            )
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _state.update {
            it?.copy(
                error=null
            )
        }
    }

    override fun onBeginningOfSpeech()=Unit

    override fun onRmsChanged(rmsdB: Float)=Unit
    override fun onBufferReceived(buffer: ByteArray?)=Unit

    override fun onEndOfSpeech() {
        _state.update {
            it?.copy(
                isSpeaking = false
            )
        }
    }

    override fun onError(error: Int) {
       if (error==SpeechRecognizer.ERROR_CLIENT){
           return
       }
        _state.update {
            it?.copy(
                error="Error $error"
            )
        }
    }

    override fun onResults(results: Bundle?) {
        results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.getOrNull(0)
            ?.let {result->
                _state.update {
                    it?.copy(
                        spokenText = result
                    )
                }
            }
    }

    override fun onPartialResults(partialResults: Bundle?)=Unit

    override fun onEvent(eventType: Int, params: Bundle?) =Unit
}