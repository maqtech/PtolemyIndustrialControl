/***preinitBlock***/
$super();
jmethodID $actorSymbol(plotSetPointsPersistence);
/**/

/***initBlock***/
$super();     
$actorSymbol(plotSetPointsPersistence) = (*env)->GetMethodID
        (env, $actorSymbol(plotClass), "setPointsPersistence", "(I)V");
(*env)->CallVoidMethod(env, $actorSymbol(plotObject), 
         $actorSymbol(plotSetPointsPersistence), $val(persistence));  
/**/


