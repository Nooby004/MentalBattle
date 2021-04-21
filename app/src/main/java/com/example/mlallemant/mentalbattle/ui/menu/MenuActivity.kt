package com.example.mlallemant.mentalbattle.ui.menu

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.MenuActivityBinding
import com.example.mlallemant.mentalbattle.ui.friends.FriendModel
import com.example.mlallemant.mentalbattle.ui.game.GameActivity
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_CURRENT_PLAYER_ID
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_GAME_ID
import com.example.mlallemant.mentalbattle.ui.login.LoginActivity
import com.example.mlallemant.mentalbattle.ui.menu.fragment.SelectorFragment
import com.example.mlallemant.mentalbattle.utils.*
import com.example.mlallemant.mentalbattle.utils.CustomDialog.OnClickBtnListener
import com.example.mlallemant.mentalbattle.utils.Utils.ONE_MEGABYTE
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

/**
 * Created by m.lallemant on 09/11/2017.
 */
class MenuActivity : AppCompatActivity() {

    private var _binding: MenuActivityBinding? = null
    private val binding get() = _binding!!

    private var friendListToFragment: FriendListToFragment? = null
    private var onBackPressedListener: OnBackPressedListener? = null

    //FireBase
    private var mAuth: FirebaseAuth? = null
    private lateinit var db: DatabaseManager
    private var storage: FirebaseStorage? = null

    //Utils
    var currentPlayer: Player? = null
        private set
    private var isFirstReqToPlay = false
    private var currentGame: Game? = null
    private var cdRequestReceived: CustomDialog? = null

    interface OnBackPressedListener {
        fun doBack()
    }

    interface FriendListToFragment {
        fun sendData(friendList: List<FriendModel>?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = MenuActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth?.currentUser
        if (Utils.AUTHENTIFICATION_TYPE != Utils.AUTHENTIFICATION_GUEST) {
            if (user != null) {
                if (user.displayName != null && user.displayName != "") {
                    db = DatabaseManager.getInstance()
                    storage = FirebaseStorage.getInstance()

                    //get data for the current user --> launch listener once to request base
                    db.getCurrentUserDataById(user.uid)
                    db.setOnDataUserUpdateListener { player_ ->
                        binding.loadingLayout.root.visibility = View.GONE
                        currentPlayer = player_
                        launchSelectorFragment()
                        db.initFriendList()
                        val splitName =
                            currentPlayer?.let { it.name.split(" ".toRegex()).toTypedArray()[0] }

                        val player = Player(
                            currentPlayer?.id,
                            splitName,
                            0,
                            currentPlayer?.nb_win,
                            currentPlayer?.nb_lose,
                            currentPlayer?.xp
                        )
                        loadProfilePicture(player)
                        db.insertPlayerInLobby(player)
                        loadProfilePicture(player)
                        initUI()
                        initListener()
                    }
                } else {
                    signOut()
                    launchLoginActivity()
                }
            } else {
                signOut()
                launchLoginActivity()
            }
        } else {
            binding.loadingLayout.root.visibility = View.GONE
            db = DatabaseManager.getInstance()
            currentPlayer = intent.getParcelableExtra(BUNDLE_EXTRA_CURRENT_PLAYER)
            if (currentPlayer != null) {
                launchSelectorFragment()
                db.insertPlayerInLobby(currentPlayer)
                initUI()
                initListener()
            } else {
                signOut()
                launchLoginActivity()
            }
        }
    }

    fun setFriendListToFragment(listener: FriendListToFragment?) {
        friendListToFragment = listener
    }

    fun setOnBackPressedListener(listener: OnBackPressedListener?) {
        onBackPressedListener = listener
    }

    public override fun onStop() {
        super.onStop()
        if (currentPlayer != null) {
            db.deletePlayerInLobby(currentPlayer)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (currentPlayer != null) {
            db.deletePlayerInLobby(currentPlayer)
        }
        _binding = null
    }

    override fun onBackPressed() {
        if (onBackPressedListener != null) {
            onBackPressedListener?.doBack()
        } else {
            super.onBackPressed()
            if (currentPlayer != null) {
                db.deletePlayerInLobby(currentPlayer)
            }
            if (Utils.AUTHENTIFICATION_TYPE == Utils.AUTHENTIFICATION_GUEST) {
                signOut()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        if (currentPlayer != null) {
            db.insertPlayerInLobby(currentPlayer)
        }
    }

    private fun initUI() {
        with(binding) {
            currentPlayer?.let {
                if (Utils.AUTHENTIFICATION_TYPE != Utils.AUTHENTIFICATION_GUEST) {

                    //SET NAME CURRENT PLAYER
                    val splitName = it.name.split(" ".toRegex()).toTypedArray()[0]
                    menuTvUsername.text = splitName

                    //Win / Lose
                    var text: String =
                        "<font color=#60c375>" + it.nb_win + " W </font><font color=#FFFFFF>/</font><font color=#FF0000> " + it.nb_lose + " L"
                    menuTvNbWinLoses.text = Html.fromHtml(text)

                    //LEVEL
                    val level = RankComputer().getLevelByXp(it.xp)
                    text = "LEVEL $level"
                    menuTvCurrentLevel.text = text

                    //CURRENT RANK
                    text = RankComputer().getRankByLevel(level, this@MenuActivity)
                    menuTvCurrentRank.text = text

                    //NEXT RANK
                    text = RankComputer().getNextRankByLevel(level, this@MenuActivity)
                    menuTvNextRank.text = text

                    //CURRENT XP + RANGE
                    val range = RankComputer().getRangeLevelByLevel(level)
                    text = it.xp.toString() + "/" + range[1] + " XP"
                    menuTvCurrentXp.text = text
                    menuPbProgressXp.max = range[1] - range[0]
                    val progress = it.xp * (range[1] - range[0]) / range[1]
                    menuPbProgressXp.progress = progress
                } else {
                    //SET NAME CURRENT PLAYER
                    val text = it.name
                    menuTvUsername.text = text
                    menuTvCurrentXp.text = getString(R.string.guest)
                    menuTvNextRank.text = getString(R.string.register_for_more)
                    menuTvNbWinLoses.visibility = View.INVISIBLE
                    menuTvCurrentLevel.visibility = View.INVISIBLE
                    menuPbProgressXp.visibility = View.INVISIBLE
                    menuTvCurrentXp.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun initListener() {
        //LOG OFF
        binding.menuIvLogout.setOnClickListener {
            db.notifyFriendsYouAreDisconnected(currentPlayer)
            signOut()
            launchLoginActivity()
        }
        if (Utils.AUTHENTIFICATION_TYPE != Utils.AUTHENTIFICATION_GUEST) {
            db.setOnFriendChangeListener { friendList ->
                handleNotification(friendList)
                friendListToFragment?.sendData(friendList)
            }
        }
    }

    private fun handleNotification(friendList: List<FriendModel>) {
        for (i in friendList.indices) {
            val friend = friendList[i]
            db.notifyFriendYouAreConnected(currentPlayer, friend.player)
            db.notifyFriendYourProgress(currentPlayer, friend.player)
            if (friend.playReq == Utils.PLAY_REQUEST_RECEIVED && !isFirstReqToPlay) {
                isFirstReqToPlay = true
                cdRequestReceived = CustomDialog(
                    this@MenuActivity,
                    friend.player?.id,
                    friend.player?.name + " wants to play with you !",
                    "PLAY", R.color.greenColor,
                    "DECLINE", R.color.redColor
                )
                cdRequestReceived?.create()
                cdRequestReceived?.setOnClickBtnListener(object : OnClickBtnListener {
                    override fun onClickBtn1() {
                        val idGame = friend.player?.id + currentPlayer?.id
                        db.getAvailableGameById(idGame)
                        db.acceptToPlayWith(currentPlayer, friend.player)
                        isFirstReqToPlay = false
                    }

                    override fun onClickBtn2() {
                        db.declineToPlayWith(currentPlayer, friend.player)
                        val idGame = friend.player?.id + currentPlayer?.id
                        db.getAvailableGameById(idGame)
                        if (currentGame != null) db.deleteAvailableGame(currentGame)
                        currentGame = null
                        cdRequestReceived?.dismiss()
                        isFirstReqToPlay = false
                    }
                })
            } else if (friend.playReq == Utils.PLAY_OK) {
                currentGame = db.currentGame
                db.insertInProgressGame(currentGame)
                db.initListenerCurrentGame(currentGame)
                db.deleteAvailableGame(currentGame)

                //Launch game
                launchGameActivity(currentGame)
                db.declineToPlayWith(currentPlayer, friend.player)
            } else if (friend.playReq == Utils.PLAY_CANCEL) {
                if (cdRequestReceived != null) {
                    if (cdRequestReceived?.isShowing() == true) {
                        cdRequestReceived?.dismiss()
                    }
                }
                isFirstReqToPlay = false
                db.resetToPlayWith(currentPlayer, friend.player)
            }
        }
    }

    private fun launchGameActivity(game: Game?) {
        val intent = Intent(this@MenuActivity, GameActivity::class.java)
        intent.putExtra(BUNDLE_EXTRA_GAME_ID, game?.id)
        intent.putExtra(BUNDLE_EXTRA_CURRENT_PLAYER_ID, currentPlayer?.id)
        startActivity(intent)
        finish()
    }

    private fun loadProfilePicture(player: Player) {
        val storageRef = storage?.reference
        val text = "profilePictures/" + player.id + ".png"
        val imagesRef = storageRef?.child(text)
        imagesRef?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener { bytes ->
            val bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.menuIvProfile.setImageBitmap(bm)
        }
    }

    private fun launchLoginActivity() {
        val intent = Intent(this@MenuActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun launchSelectorFragment() {
        val sf = SelectorFragment()
        supportFragmentManager.beginTransaction().add(R.id.menu_fl_select, sf).commit()
    }

    private fun signOut() {
        if (currentPlayer != null) db.notifyFriendsYouAreDisconnected(currentPlayer)
        db.initFriendList()
        mAuth?.signOut()
        LoginManager.getInstance().logOut()
    }

    companion object {
        const val BUNDLE_EXTRA_CREATOR = "creator"
        const val BUNDLE_EXTRA_NAME = "name"
        const val BUNDLE_EXTRA_PASSWORD = "password"
        const val BUNDLE_EXTRA_IS_CREATOR = "isCreator"
        const val BUNDLE_EXTRA_SESSION = "session"
        const val BUNDLE_EXTRA_CURRENT_PLAYER = "currentPlayer"
    }
}