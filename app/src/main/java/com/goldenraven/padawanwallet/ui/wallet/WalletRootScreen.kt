/*
 * Copyright 2020-2022 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.goldenraven.padawanwallet.ui.wallet

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import com.goldenraven.padawanwallet.R
import com.goldenraven.padawanwallet.data.tx.Tx
import com.goldenraven.padawanwallet.theme.*
import com.goldenraven.padawanwallet.ui.FadedVerticalDivider
import com.goldenraven.padawanwallet.ui.Screen
import com.goldenraven.padawanwallet.ui.standardBorder
import io.ktor.http.*

// TODO Think about reintroducing refreshing
// TODO Reuse composable more
// TODO Handle no internet connection situations
// TODO Handle old faucet dialog
// TODO Finish up send & receive screen

@Composable
internal fun WalletRootScreen(
    navController: NavHostController,
    walletViewModel: WalletViewModel
) {
    val balance by walletViewModel.balance.observeAsState()
    val isRefreshing by walletViewModel.isRefreshing.collectAsState()
    val transactionList by walletViewModel.readAllData.observeAsState(initial = emptyList())
    val openFaucetDialog = walletViewModel.openFaucetDialog
    val context = LocalContext.current

    // if (openFaucetDialog.value) {
    // // if (true) {
    //     FaucetDialog(
    //         walletViewModel = walletViewModel
    //     )
    // }

    // if (walletViewModel.isOnline(context = context) && !Wallet.blockchainIsInitialized()) {
    //     Wallet.createBlockchain()
    // }

    Column(modifier = Modifier.standardBackground()) {
        BalanceBox(balance = balance.toString(), viewModel = walletViewModel)
        Spacer(modifier = Modifier.height(height = 12.dp))
        SendReceive(navController = navController)
        TransactionListBox(openFaucetDialog = openFaucetDialog, transactionList = transactionList)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceBox(
    balance: String,
    viewModel: WalletViewModel
) {
    val context = LocalContext.current // TODO: is this the right place to get this context?
    Card(
        border = standardBorder,
        shape = RoundedCornerShape(20.dp),
        containerColor = padawan_theme_onBackground_secondary,
        modifier = Modifier
            .standardShadow(20.dp)
            .fillMaxWidth()
    ) {
        ConstraintLayout(
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 0.dp)
                .fillMaxWidth()
        ) {
            val (cardName, currencyToggle, balanceText, currencyText, buttonRow) = createRefs()
            val currencyToggleState = remember { mutableStateOf(value = false) }
            Text(
                text = "bitcoin testnet",
                style = PadawanTypography.bodyMedium,
                color = padawan_theme_text_faded,
                modifier = Modifier.constrainAs(cardName) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
            )
            Box(
                modifier = Modifier
                    .noRippleClickable { currencyToggleState.value = !currencyToggleState.value }
                    .background(
                        color = padawan_theme_button_secondary,
                        shape = RoundedCornerShape(size = 10.dp)
                    )
                    .constrainAs(currencyToggle) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = 8.dp)
                ) {
                    CurrencyToggleText(currencyToggleState = currencyToggleState, text = CurrencyType.BTC)
                    FadedVerticalDivider()
                    CurrencyToggleText(currencyToggleState = currencyToggleState, text = CurrencyType.SATS)
                }
            }
            Text(
                // text = "100",
                text = balance,
                style = PadawanTypography.displaySmall,
                fontSize = 36.sp,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .constrainAs(balanceText) {
                        top.linkTo(cardName.bottom)
                        start.linkTo(parent.start)
                    }
            )
            Text(
                text = CurrencyType.SATS.toString().lowercase(),
                style = PadawanTypography.bodyMedium,
                modifier = Modifier
                    .padding(all = 8.dp)
                    .constrainAs(currencyText) {
                        start.linkTo(balanceText.end)
                        bottom.linkTo(balanceText.bottom)
                    }
            )
            Row(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 0.dp)
                    .constrainAs(buttonRow) {
                        top.linkTo(balanceText.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Button(
                    onClick = {
                        viewModel.refresh(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 0.dp),
                    border = standardBorder,
                    modifier = Modifier
                        .width(110.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(
                            text = "sync",
                            style = PadawanTypography.labelLarge,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xffdbdeff),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sync),
                            tint = Color(0xffdbdeff),
                            contentDescription = "Sync icon"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SendReceive(navController: NavHostController) {
    Row(
        modifier = Modifier
            .padding(top = 4.dp)
    ) {
        Button(
            onClick = { navController.navigate(Screen.ReceiveScreen.route) },
            colors = ButtonDefaults.buttonColors(containerColor = padawan_theme_button_secondary),
            shape = RoundedCornerShape(20.dp),
            border = standardBorder,
            modifier = Modifier
                .padding(all = 4.dp)
                .standardShadow(20.dp)
                .weight(weight = 0.5f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Receive",
                    style = PadawanTypography.labelLarge,
                )
                Icon(painter = painterResource(id = R.drawable.ic_receive), contentDescription = "Receive Icon")
            }
        }
        Button(
            onClick = { navController.navigate(Screen.SendScreen.route) },
            colors = ButtonDefaults.buttonColors(containerColor = padawan_theme_button_primary),
            shape = RoundedCornerShape(20.dp),
            border = standardBorder,
            modifier = Modifier
                .padding(all = 4.dp)
                .standardShadow(20.dp)
                .weight(weight = 0.5f),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Send",
                    style = PadawanTypography.labelLarge,
                )
                Icon(painter = painterResource(id = R.drawable.ic_send), contentDescription = "Send Icon")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListBox(openFaucetDialog: MutableState<Boolean>, transactionList: List<Tx>) {
    Row(modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)) {
        Text(
            text = "Transactions",
            style = PadawanTypography.headlineSmall,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .align(Alignment.Bottom)
                .weight(weight = 0.5f)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = standardBorder,
        shape = RoundedCornerShape(20.dp),
        containerColor = padawan_theme_background_secondary
    ) {
        if (transactionList.isEmpty()) {
            Row(modifier = Modifier.padding(all = 24.dp)) {
                Column {
                    Text(
                        text = "Hey! Your transaction list is empty, get some coins so you can start sending them!",
                        style = PadawanTypography.bodyMedium,
                        modifier = Modifier.padding(all = 8.dp)
                    )
                    Button(
                        onClick = { openFaucetDialog.value = true },
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .standardShadow(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = padawan_theme_button_primary),
                        shape = RoundedCornerShape(20.dp),
                        border = standardBorder
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(text = "Get coins", style = PadawanTypography.bodyMedium)
                            Icon(painter = painterResource(id = R.drawable.ic_receive_secondary), contentDescription = "Get Coins Icon")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .background(color = padawan_theme_lazyColumn_background)
                    .padding(horizontal = 24.dp)
            ) {
                itemsIndexed(transactionList) { index, tx ->
                    if (index == 0) {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    Column {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${tx.txid.take(n = 5)}.....${tx.txid.takeLast(n = 5)}",
                                style = PadawanTypography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                // fontFamily = ShareTechMono,
                                maxLines = 1,
                                modifier = Modifier.align(Alignment.BottomStart).padding(top = 8.dp)
                            )
                            Text(
                                text = "${if (tx.isPayment) tx.valueOut.toString() else tx.valueIn.toString()} ${CurrencyType.SATS.toString().lowercase()}",
                                style = PadawanTypography.bodyMedium,
                                textAlign = TextAlign.End,
                                modifier = Modifier.align(Alignment.BottomEnd)
                            )
                        }
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)) {
                            // val message = if (tx.date == "Pending") "Pending" else "${getDateDifference(date = tx.date)} ago"
                            Text(
                                text = tx.date,
                                style = PadawanTypography.bodySmall,
                                maxLines = 1,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .background(
                                            color = if (tx.isPayment) padawan_theme_send_primary else padawan_theme_receive_primary,
                                            shape = RoundedCornerShape(size = 5.dp)
                                        )
                                ) {
                                    Text(
                                        text = if (tx.isPayment) "Sent" else "Receive",
                                        style = PadawanTypography.bodySmall,
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                                    )
                                    Icon(
                                        painter = if (tx.isPayment) painterResource(id = R.drawable.ic_send_secondary) else painterResource(id = R.drawable.ic_receive_secondary),
                                        tint = padawan_disabled,
                                        contentDescription = if (tx.isPayment) "Send Icon" else "Receive Icon",
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .scale(scale = 0.75f)
                                            .padding(end = 8.dp),
                                    )
                                }
                            }
                        }
                        if (index != transactionList.size - 1) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        } else {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyToggleText(currencyToggleState: MutableState<Boolean>, text: CurrencyType) {
    val currencyState = (!currencyToggleState.value && text == CurrencyType.BTC) || (currencyToggleState.value && text == CurrencyType.SATS)

    val colorTransition = updateTransition(
        if (currencyState) padawan_theme_onBackground_faded else padawan_theme_onPrimary, label = "Currency Toggle Text"
    )
    val color by colorTransition.animateColor(
        transitionSpec = { tween(durationMillis = 500) },
        label = "Changing Color Animation",
    ) {
        if (it == padawan_theme_onBackground_faded) padawan_theme_onPrimary else padawan_theme_onBackground_faded
    }

    Text(
        text = text.toString().lowercase(),
        textAlign = TextAlign.Center,
        style = PadawanTypography.bodyMedium,
        color = color,
        modifier = Modifier.padding(all = 8.dp),
    )
}

@Composable
private fun FaucetDialog(walletViewModel: WalletViewModel) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = "Hello there!",
                style = PadawanTypography.headlineMedium,
                color = Color(0xff1f0208)
            )
        },
        text = {
            Text(
                text = "We notice it is your first time opening Padawan wallet. Would you like Padawan to send you some testnet coins to get you started?",
                color = Color(0xff2f2f2f)
            )
        },

        dismissButton = {
            Button(
                onClick = {
                    walletViewModel.onNegativeDialogClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffc4f4f)),
                shape = RoundedCornerShape(20.dp),
                border = standardBorder,
                modifier = Modifier
                    .padding(top = 4.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
                    .standardShadow(20.dp)
                    .height(70.dp)
                    .width(110.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Not now",
                        style = PadawanTypography.labelMedium,
                        fontSize = 12.sp,
                        color = Color(0xff000000)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_hicon_dislike),
                        contentDescription = "Do not log in icon",
                        tint = Color(0xff000000)
                    )
                }
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    walletViewModel.onPositiveDialogClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff78c5b2)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(0.dp),
                border = standardBorder,
                modifier = Modifier
                    .padding(top = 4.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
                    .standardShadow(20.dp)
                    .height(70.dp)
                    .width(110.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Yes please!",
                        style = PadawanTypography.labelMedium,
                        fontSize = 12.sp,
                        color = Color(0xff000000)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_hicon_like),
                        contentDescription = "Proceed icon",
                        tint = Color(0xff000000)
                    )
                }
            }
        },
    )
}