package com.tupausa.view

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tupausa.R

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun screenWelcome(navController: NavController){

    val imagePainter: Painter = painterResource(id = R.drawable.fondo)
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {}
            Image(
                painter = imagePainter,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
        }

    }

    var MainColor = Color(0xFF615555)
    val context = LocalContext.current
    /*val repository = "https://github.com/Chocolatamargo2607/UD.PPC.MyPlantSitter"
    val repositoryintent = remember { Intent(Intent.ACTION_VIEW, Uri.parse(repository)) }*/

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Image(painter = painterResource(id = R.drawable.logo), contentDescription ="LogoTuPausa",
            modifier = Modifier.size(400.dp))
        /*Button(
            onClick = {
                if (FirebaseAuth.getInstance().currentUser?.email.isNullOrEmpty()) {
                    navController.navigate(route = appScreens.screenLogin.router)
                } else {
                    navController.navigate(route = appScreens.tabsMovements.router)
                }
            },
            colors = ButtonDefaults.buttonColors(
                MainColor,
                contentColor = Color.White
            )
        ) {
            Text(text = stringResource(R.string.button_log_in))
        }*/
        Spacer(modifier = Modifier.width(200.dp))

        /*IconButton(onClick ={ context.startActivity(repositoryintent)}) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = MainColor
            )

        }*/


    }
}

@Preview(showBackground = true)
@Composable
fun screenWelcomepreview() {
    screenWelcome(NavController(LocalContext.current))
}