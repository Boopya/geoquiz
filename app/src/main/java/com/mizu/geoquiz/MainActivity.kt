package com.mizu.geoquiz

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import kotlin.math.roundToInt

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {
    
    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button
    private lateinit var cheatButton: Button
    private lateinit var questionTextView: TextView

    private val quizViewModel: QuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: called.")
        setContentView(R.layout.activity_main)

        Log.d(TAG, "In onCreate: QuizViewModel is $quizViewModel")

        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        quizViewModel.currentIndex = currentIndex

        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        prevButton = findViewById(R.id.prev_button)
        cheatButton = findViewById(R.id.cheat_button)
        questionTextView = findViewById(R.id.question_text_view)

        trueButton.setOnClickListener { view: View ->
            checkAnswer(true)
            markCurrentQuestionAsAnswered()
            disableAnswerButtonsIfCurrentQuestionIsAnswered()
            displayPercentageScoreIfAllQuestionsAreAnswered()
        }

        falseButton.setOnClickListener { view: View ->
            checkAnswer(false)
            markCurrentQuestionAsAnswered()
            disableAnswerButtonsIfCurrentQuestionIsAnswered()
            displayPercentageScoreIfAllQuestionsAreAnswered()
        }

        nextButton.setOnClickListener {
            quizViewModel.moveToNext()
            updateQuestion()
            disableAnswerButtonsIfCurrentQuestionIsAnswered()
        }

        prevButton.setOnClickListener {
            quizViewModel.moveToPrev()
            updateQuestion()
            disableAnswerButtonsIfCurrentQuestionIsAnswered()
        }

        cheatButton.setOnClickListener { view ->
            // Start CheatActivity
            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val options = ActivityOptions
                    .makeClipRevealAnimation(view, 0, 0, view.width, view.height)
                startActivityForResult(intent, REQUEST_CODE_CHEAT, options.toBundle())
            } else {
                startActivityForResult(intent, REQUEST_CODE_CHEAT)
            }
        }

        questionTextView.setOnClickListener {
            quizViewModel.moveToNext()
            updateQuestion()
        }

        updateQuestion()
        disableAnswerButtonsIfCurrentQuestionIsAnswered()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            quizViewModel.isCheater =
                data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: called.")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: called.")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: called.")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(TAG, "onSaveInstanceState: called.")
        outState.putInt(KEY_INDEX, quizViewModel.currentIndex)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: called.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: called.")
    }

    private fun updateQuestion() {
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)
    }

    private fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = quizViewModel.currentQuestionAnswer
        val messageResId: Int?

        when {
            quizViewModel.isCheater -> messageResId = R.string.judgement_toast
            userAnswer == correctAnswer -> {
                messageResId = R.string.correct_toast
                quizViewModel.score++
            }
            else -> messageResId = R.string.incorrect_toast
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT)
            .show()
    }

    private fun disableAnswerButtonsIfCurrentQuestionIsAnswered() {
        trueButton.isEnabled = !quizViewModel.isCurrentQuestionAnswered
        falseButton.isEnabled = !quizViewModel.isCurrentQuestionAnswered
    }

    private fun markCurrentQuestionAsAnswered() {
        quizViewModel.currentQuestion.isAnswered = true
    }

    /**
     * Loop through the question bank and check if every question has already been answered.
     * If all have already been answered, display a Toast with the percentage score.
     * Otherwise, return from the function.
     *
     * In other words, if any of the questions hasn't been answered yet, do not proceed with
     * displaying of the percentage score.
     */
    private fun displayPercentageScoreIfAllQuestionsAreAnswered() {
        val questions = quizViewModel.getQuestions()

        for (question in questions) {
            if (!question.isAnswered) {
                return
            }
        }

        val finalScore = quizViewModel.score.toDouble().div(questions.size).times(100).roundToInt()
        Log.d(TAG, "In displayPercentageScoreIfAllQuestionsAreAnswered: Final score is $finalScore%.")
        Toast.makeText(this, "Score: ${finalScore}%", Toast.LENGTH_SHORT)
            .show()
    }
}