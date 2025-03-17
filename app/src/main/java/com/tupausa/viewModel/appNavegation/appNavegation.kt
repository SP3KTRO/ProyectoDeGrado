package com.tupausa.viewModel.appNavegation

import android.os.Build

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tupausa.view.screenWelcome

/*import com.example.udppcmyplantsitter.view.screenAccount
import com.example.udppcmyplantsitter.view.screenLogin
import com.example.udppcmyplantsitter.view.screenMain
import com.example.udppcmyplantsitter.view.modalMyPlant
import com.example.udppcmyplantsitter.view.screenMyPlants
import com.example.udppcmyplantsitter.view.screenPlants
import com.udppcmyplantsitter.view.screenRegister
import com.udppcmyplantsitter.view.screenRegisterAssignment
import com.udppcmyplantsitter.view.screenRegisterMyPlants*/


@Composable
fun appNavegation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = appScreens.screenWelcome.router){
        composable(route= appScreens.screenWelcome.router){ screenWelcome(navController)}
        /*composable(route= appScreens.screenLogin.router){ screenLogin(navController) }
        composable(route= appScreens.screenRegister.router){ screenRegister(navController) }
        composable(route= appScreens.screenMain.router){ screenMain(navController) }
        composable(route= appScreens.screenAccount.router){ screenAccount(navController) }
        composable(route= appScreens.screenPlants.router){ screenPlants(navController) }
        composable(route= appScreens.screenMyPlants.router){ screenMyPlants(navController) }
        composable(route= appScreens.screenAssignment.router){ screenRegisterAssignment(navController)}
        composable(route= appScreens.tabsMovements.router){ tabsMovements(navController) }
        composable(route= appScreens.screenRegisterMyPlants.router){ screenRegisterMyPlants(navController) }
        composable(route= appScreens.modalMyPlant.router){ modalMyPlant(navController) }*/
    }
}