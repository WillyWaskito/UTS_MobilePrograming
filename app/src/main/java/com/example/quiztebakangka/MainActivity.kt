package com.example.quiztebakangka

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var menuPanel: LinearLayout
    private lateinit var gamePanel: LinearLayout
    private lateinit var scoreText: TextView
    private lateinit var attemptText: TextView
    private lateinit var feedbackText: TextView
    private lateinit var rangeText: TextView
    private lateinit var bestScoreText: TextView
    private lateinit var guessInput: EditText
    private lateinit var guessButton: Button

    private var secretNumber = 0
    private var score = INITIAL_SCORE
    private var attempts = 0
    private var gameFinished = false
    private var inGame = false

    // Rentang yang masih mungkin (dipersempit berdasarkan petunjuk)
    private var low = MIN_NUMBER
    private var high = MAX_NUMBER

    private var feedbackRes = R.string.feedback_start
    private var feedbackColor = COLOR_NEUTRAL
    private var feedbackOverride: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        menuPanel = findViewById(R.id.menuPanel)
        gamePanel = findViewById(R.id.gamePanel)
        scoreText = findViewById(R.id.scoreText)
        attemptText = findViewById(R.id.attemptText)
        feedbackText = findViewById(R.id.feedbackText)
        rangeText = findViewById(R.id.rangeText)
        bestScoreText = findViewById(R.id.bestScoreText)
        guessInput = findViewById(R.id.guessInput)
        guessButton = findViewById(R.id.guessButton)

        findViewById<Button>(R.id.startButton).setOnClickListener { startNewGame() }
        findViewById<Button>(R.id.aboutButton).setOnClickListener { showAbout() }
        findViewById<Button>(R.id.helpButton).setOnClickListener { showHelp() }
        findViewById<Button>(R.id.restartButton).setOnClickListener { confirmRestart() }
        findViewById<Button>(R.id.homeButton).setOnClickListener { confirmLeave() }
        guessButton.setOnClickListener { submitGuess() }
        guessInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitGuess()
                true
            } else false
        }

        if (savedInstanceState != null) restoreState(savedInstanceState)
        render()
    }

    // ---------------------------------------------------------------- state

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SECRET, secretNumber)
        outState.putInt(KEY_SCORE, score)
        outState.putInt(KEY_ATTEMPTS, attempts)
        outState.putInt(KEY_LOW, low)
        outState.putInt(KEY_HIGH, high)
        outState.putBoolean(KEY_FINISHED, gameFinished)
        outState.putBoolean(KEY_IN_GAME, inGame)
        outState.putInt(KEY_FEEDBACK_RES, feedbackRes)
        outState.putInt(KEY_FEEDBACK_COLOR, feedbackColor)
        outState.putString(KEY_FEEDBACK_TEXT, feedbackOverride)
    }

    private fun restoreState(state: Bundle) {
        secretNumber = state.getInt(KEY_SECRET)
        score = state.getInt(KEY_SCORE, INITIAL_SCORE)
        attempts = state.getInt(KEY_ATTEMPTS)
        low = state.getInt(KEY_LOW, MIN_NUMBER)
        high = state.getInt(KEY_HIGH, MAX_NUMBER)
        gameFinished = state.getBoolean(KEY_FINISHED)
        inGame = state.getBoolean(KEY_IN_GAME)
        feedbackRes = state.getInt(KEY_FEEDBACK_RES, R.string.feedback_start)
        feedbackColor = state.getInt(KEY_FEEDBACK_COLOR, COLOR_NEUTRAL)
        feedbackOverride = state.getString(KEY_FEEDBACK_TEXT)
    }

    /** Menyelaraskan seluruh tampilan dengan state saat ini. */
    private fun render() {
        scoreText.text = score.toString()
        attemptText.text = attempts.toString()
        feedbackText.text = feedbackOverride ?: getString(feedbackRes)
        feedbackText.setTextColor(feedbackColor)
        rangeText.text = if (inGame && !gameFinished && (low != MIN_NUMBER || high != MAX_NUMBER)) {
            getString(R.string.game_range_format, low, high)
        } else {
            getString(R.string.game_range_default)
        }
        setInputEnabled(!gameFinished)
        menuPanel.visibility = if (inGame) View.GONE else View.VISIBLE
        gamePanel.visibility = if (inGame) View.VISIBLE else View.GONE
        updateBestScoreLabel()
    }

    private fun setInputEnabled(enabled: Boolean) {
        guessInput.isEnabled = enabled
        guessButton.isEnabled = enabled
        guessButton.alpha = if (enabled) 1f else 0.55f
    }

    private fun setFeedback(resId: Int, color: Int, text: String? = null) {
        feedbackRes = resId
        feedbackColor = color
        feedbackOverride = text
    }

    // ----------------------------------------------------------------- game

    private fun startNewGame() {
        secretNumber = (MIN_NUMBER..MAX_NUMBER).random()
        score = INITIAL_SCORE
        attempts = 0
        low = MIN_NUMBER
        high = MAX_NUMBER
        gameFinished = false
        inGame = true
        setFeedback(R.string.feedback_start, COLOR_NEUTRAL)
        guessInput.setText("")
        guessInput.error = null
        render()
        guessInput.requestFocus()
    }

    private fun submitGuess() {
        if (gameFinished) {
            Toast.makeText(this, R.string.feedback_finished, Toast.LENGTH_SHORT).show()
            return
        }

        val raw = guessInput.text.toString().trim()
        if (raw.isEmpty()) {
            guessInput.error = getString(R.string.error_empty)
            guessInput.requestFocus()
            return
        }

        val guess = raw.toIntOrNull()
        if (guess == null || guess !in MIN_NUMBER..MAX_NUMBER) {
            guessInput.error = getString(R.string.error_range)
            guessInput.requestFocus()
            return
        }

        hideKeyboard()
        attempts++

        if (guess == secretNumber) {
            onWin()
            return
        }

        score = (score - PENALTY).coerceAtLeast(0)
        if (guess < secretNumber) {
            low = maxOf(low, guess + 1)
            setFeedback(R.string.feedback_too_low, COLOR_WRONG)
        } else {
            high = minOf(high, guess - 1)
            setFeedback(R.string.feedback_too_high, COLOR_WRONG)
        }
        guessInput.setText("")

        if (score == 0) onLose() else render()
    }

    private fun onWin() {
        gameFinished = true
        setFeedback(
            0, COLOR_CORRECT,
            getString(R.string.feedback_win_format, secretNumber, score)
        )
        val isRecord = saveBestScore(score)
        render()

        var message = getString(R.string.dialog_win_message_format, secretNumber, score, attempts)
        if (isRecord) message += getString(R.string.dialog_win_record)
        showEndDialog(getString(R.string.dialog_win_title), message)
    }

    private fun onLose() {
        gameFinished = true
        setFeedback(
            0, COLOR_WRONG,
            getString(R.string.feedback_lose_format, secretNumber)
        )
        render()
        showEndDialog(
            getString(R.string.dialog_lose_title),
            getString(R.string.dialog_lose_message_format, secretNumber)
        )
    }

    private fun showEndDialog(title: String, message: String) {
        if (isFinishing) return
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.dialog_play_again) { _, _ -> startNewGame() }
            .setNegativeButton(R.string.dialog_back_to_menu) { _, _ -> showMenu() }
            .setCancelable(false)
            .show()
    }

    // ----------------------------------------------------------- navigation

    private fun confirmRestart() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_restart_title)
            .setMessage(R.string.dialog_restart_message)
            .setPositiveButton(R.string.dialog_restart_yes) { _, _ -> startNewGame() }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    /** Konfirmasi hanya jika permainan masih berjalan. */
    private fun confirmLeave() {
        if (!inGame || gameFinished) {
            showMenu()
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_leave_title)
            .setMessage(R.string.dialog_leave_message)
            .setPositiveButton(R.string.dialog_leave_yes) { _, _ -> showMenu() }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    private fun showMenu() {
        hideKeyboard()
        inGame = false
        gameFinished = false
        render()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (inGame) confirmLeave() else super.onBackPressed()
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle(R.string.about_title)
            .setMessage(R.string.about_message)
            .setPositiveButton(R.string.about_ok, null)
            .show()
    }

    private fun showHelp() {
        AlertDialog.Builder(this)
            .setTitle(R.string.help_title)
            .setMessage(R.string.help_message)
            .setPositiveButton(R.string.help_ok, null)
            .show()
    }

    // ----------------------------------------------------------- best score

    private fun prefs() = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** @return true bila [newScore] adalah rekor baru. */
    private fun saveBestScore(newScore: Int): Boolean {
        val best = prefs().getInt(KEY_BEST, -1)
        if (newScore > best) {
            prefs().edit().putInt(KEY_BEST, newScore).apply()
            return true
        }
        return false
    }

    private fun updateBestScoreLabel() {
        val best = prefs().getInt(KEY_BEST, -1)
        bestScoreText.text = if (best < 0) {
            getString(R.string.best_score_none)
        } else {
            getString(R.string.best_score_format, best)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(guessInput.windowToken, 0)
        guessInput.clearFocus()
    }

    private companion object {
        const val MIN_NUMBER = 0
        const val MAX_NUMBER = 100
        const val INITIAL_SCORE = 100
        const val PENALTY = 10

        val COLOR_NEUTRAL = Color.rgb(100, 116, 139)
        val COLOR_CORRECT = Color.rgb(22, 167, 101)
        val COLOR_WRONG = Color.rgb(229, 72, 93)

        const val PREFS_NAME = "quiz_tebak_angka"
        const val KEY_BEST = "best_score"

        const val KEY_SECRET = "secret"
        const val KEY_SCORE = "score"
        const val KEY_ATTEMPTS = "attempts"
        const val KEY_LOW = "low"
        const val KEY_HIGH = "high"
        const val KEY_FINISHED = "finished"
        const val KEY_IN_GAME = "in_game"
        const val KEY_FEEDBACK_RES = "feedback_res"
        const val KEY_FEEDBACK_COLOR = "feedback_color"
        const val KEY_FEEDBACK_TEXT = "feedback_text"
    }
}
