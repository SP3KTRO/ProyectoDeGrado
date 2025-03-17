package com.tupausa.viewModel.appNavegation

sealed class appScreens (val router: String){
    object screenWelcome: appScreens("screenWelcome")
    object screenLogin: appScreens("screenLogin")
    object screenRegister: appScreens("screenRegister")
    object screenMain: appScreens("screenMain")
    object screenAccount: appScreens("screenAccount")
    object screenPlants: appScreens("screenPlants")
    object screenMyPlants: appScreens("screenMyPlants")
    object screenAssignment: appScreens("screenAssignment")
    object tabsMovements: appScreens("tabsMovements")
    object modalMyPlant: appScreens("modalMyPlant")
    object screenRegisterMyPlants: appScreens("screenRegisterMyPlants")

}