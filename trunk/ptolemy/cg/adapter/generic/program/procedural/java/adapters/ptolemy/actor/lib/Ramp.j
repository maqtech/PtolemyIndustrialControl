/***preinitBlock***/
static $targetType(output) $actorSymbol(state);
/**/

/*** ArrayConvertInitBlock($elementType) ***/
$actorSymbol(state) = $typeFunc(TYPE_Array::convert($actorSymbol(state), $elementType));
/**/

/*** ArrayConvertStepBlock($elementType) ***/
$param(step) = $typeFunc(TYPE_Array::convert($param(step), $elementType)));
/**/

/***CommonInitBlock($type)***/
$actorSymbol(state) = $val(($type)init);
/**/

/***StringInitBlock***/
$actorSymbol(state) = $val((String)init);
/**/

/***IntegerFireBlock***/
$put(output, $actorSymbol(state));
if ($hasToken(step)) {
        $param(step) = $get(step);
}
$actorSymbol(state) += (Integer)$param(step);
/**/

/***DoubleFireBlock***/
$put(output, $actorSymbol(state));
if ($hasToken(step)) {
        $param(step) = $get(step);
}
$actorSymbol(state) += (Double)$param(step);
/**/

/***BooleanFireBlock***/
$put(output, $actorSymbol(state));
if ($hasToken(step)) {
        $param(step) = $get(step);
}
$actorSymbol(state) |= (Boolean)$param(step);
/**/

/***StringFireBlock***/
// Ramp StringFireBlock start
$put(output, $actorSymbol(state));
$actorSymbol(state) = $actorSymbol(state) + $param(step);
// Ramp StringFireBlock end
/**/

/***TokenFireBlock***/
$put(output, $actorSymbol(state));
if ($hasToken(step)) {
        $param(step) = $get(step);
}
$actorSymbol(state) = $add_Token_Token($actorSymbol(state), $param(step));
/**/
