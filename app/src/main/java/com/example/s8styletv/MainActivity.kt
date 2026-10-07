package com.example.s8styletv
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private val Bg=Color(0xFF080B12); private val Panel=Color(0xFF111827); private val Accent=Color(0xFF2F80ED)
private data class Channel(val name:String,val group:String,val streamUrl:String,val favorite:Boolean=false)
private data class Movie(val title:String,val year:String,val description:String,val streamUrl:String)
private val publicChannels=listOf(
 Channel("NASA+","Science","https://ntv1.akamaized.net/hls/live/2018450/NASA-NTV1/master.m3u8"),
 Channel("NASA Media","Science","https://ntv2.akamaized.net/hls/live/2018451/NASA-NTV2/master.m3u8")
)
class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{App()}}}
@Composable fun App(){
 var selected by remember{mutableStateOf("Home")}; var title by remember{mutableStateOf<String?>(null)}; var url by remember{mutableStateOf<String?>(null)}; var channels by remember{mutableStateOf(publicChannels)}
 val nav=listOf("Home","Live TV","Guide","Movies","Favorites","Video Add-ons","Settings")
 MaterialTheme(colorScheme=darkColorScheme(background=Bg,surface=Panel,primary=Accent)){
  if(url!=null) Player(title?:"Playing",url!!){url=null;title=null} else Column(Modifier.fillMaxSize().background(Bg).padding(28.dp)){
   Header();Spacer(Modifier.height(20.dp));LazyRow(horizontalArrangement=Arrangement.spacedBy(10.dp)){items(nav){n->Nav(n,n==selected){selected=n}}};Spacer(Modifier.height(24.dp))
   when(selected){
    "Home"->Home(channels){title=it.name;url=it.streamUrl}
    "Live TV"->Live(channels,{title=it.name;url=it.streamUrl}){c->channels=channels.map{if(it.name==c.name)it.copy(favorite=!it.favorite)else it}}
    "Guide"->Guide(channels)
    "Movies"->Movies{title=it.title;url=it.streamUrl}
    "Favorites"->Live(channels.filter{it.favorite},{title=it.name;url=it.streamUrl}){c->channels=channels.map{if(it.name==c.name)it.copy(favorite=!it.favorite)else it}}
    "Video Add-ons"->VideoAddons()
    else->Settings()
   }
  }
 }
}
@Composable private fun Header(){Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.fillMaxWidth()){Text("THE BAKER\'S",color=Accent,fontSize=30.sp,fontWeight=FontWeight.Black);Spacer(Modifier.width(8.dp));Text("DOZEN",color=Color.White,fontSize=24.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.weight(1f));Text("FREE & PUBLIC MEDIA",color=Color.LightGray,fontSize=14.sp)}}
@Composable private fun Nav(s:String,on:Boolean,click:()->Unit){var f by remember{mutableStateOf(false)};Button(click,Modifier.onFocusChanged{f=it.isFocused}.focusable(),colors=ButtonDefaults.buttonColors(containerColor=if(on||f)Accent else Panel)){Text(s)}}
@Composable private fun Home(cs:List<Channel>,play:(Channel)->Unit){Column{Text("Watch Free & Public Media",color=Color.White,fontSize=30.sp,fontWeight=FontWeight.Bold);Text("Live channels, public-domain movies and guide data",color=Color.Gray);Spacer(Modifier.height(18.dp));Text("Live Now",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));LazyRow(horizontalArrangement=Arrangement.spacedBy(14.dp)){items(cs){c->Tile(c,play)}};Spacer(Modifier.height(20.dp));Text("Open Movies to load a fresh public-domain catalog.",color=Color.LightGray)}}
@Composable private fun Tile(c:Channel,play:(Channel)->Unit){var f by remember{mutableStateOf(false)};Card({play(c)},Modifier.width(220.dp).height(120.dp).onFocusChanged{f=it.isFocused},colors=CardDefaults.cardColors(containerColor=if(f)Accent else Panel)){Box(Modifier.fillMaxSize().padding(14.dp),contentAlignment=Alignment.BottomStart){Text(c.name,color=Color.White,fontWeight=FontWeight.Bold)}}}
@Composable private fun Live(cs:List<Channel>,play:(Channel)->Unit,fav:(Channel)->Unit){Column{Text("Live TV",color=Color.White,fontSize=30.sp,fontWeight=FontWeight.Bold);Text("Public broadcaster feeds",color=Color.Gray);Spacer(Modifier.height(14.dp));if(cs.isEmpty())Text("No favorite channels yet.",color=Color.Gray);LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){items(cs){c->Row(Modifier.fillMaxWidth().height(72.dp).background(Panel,RoundedCornerShape(10.dp)).padding(horizontal=18.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(c.name,color=Color.White,fontWeight=FontWeight.Bold);Text(c.group,color=Color.LightGray)};TextButton({fav(c)}){Text(if(c.favorite)"★" else "☆",fontSize=24.sp)};Button({play(c)}){Text("Watch")}}}}}}
@Composable private fun Player(t:String,u:String,back:()->Unit){val ctx=androidx.compose.ui.platform.LocalContext.current;val p=remember(u){ExoPlayer.Builder(ctx).build().apply{setMediaItem(MediaItem.fromUri(Uri.parse(u)));prepare();playWhenReady=true}};DisposableEffect(p){onDispose{p.release()}};Box(Modifier.fillMaxSize().background(Color.Black)){AndroidView({PlayerView(it).apply{player=p;useController=true}},Modifier.fillMaxSize());Button(back,Modifier.padding(24.dp).align(Alignment.TopStart)){Text("Back")};Text(t,color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(24.dp).align(Alignment.BottomStart))}}
@Composable private fun Movies(play:(Movie)->Unit){var ms by remember{mutableStateOf<List<Movie>>(emptyList())};var status by remember{mutableStateOf("Loading public-domain movies…")};LaunchedEffect(Unit){runCatching{archiveMovies()}.onSuccess{ms=it;status="Internet Archive • "+it.size+" titles"}.onFailure{status="Catalog error: "+(it.message?:"network error")}};Column{Text("Movies",color=Color.White,fontSize=30.sp,fontWeight=FontWeight.Bold);Text(status,color=Color.Gray);Spacer(Modifier.height(14.dp));LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){items(ms){m->Row(Modifier.fillMaxWidth().background(Panel,RoundedCornerShape(10.dp)).padding(16.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(m.title,color=Color.White,fontWeight=FontWeight.Bold,fontSize=18.sp);Text(m.year,color=Color.LightGray);if(m.description.isNotBlank())Text(m.description.take(130),color=Color.Gray,maxLines=2)};Button({play(m)}){Text("Play")}}}}}}
private suspend fun archiveMovies(): List<Movie> = withContext(Dispatchers.IO){val q=URLEncoder.encode("mediatype:movies AND collection:feature_films","UTF-8");val docs=JSONObject(get("https://archive.org/advancedsearch.php?q=$q&fl[]=identifier,title,year,description&rows=20&page=1&output=json")).getJSONObject("response").getJSONArray("docs");val out=mutableListOf<Movie>();for(i in 0 until docs.length()){val d=docs.getJSONObject(i);val id=d.optString("identifier");if(id.isBlank())continue;runCatching{val files=JSONObject(get("https://archive.org/metadata/"+URLEncoder.encode(id,"UTF-8"))).getJSONArray("files");var chosen="";for(j in 0 until files.length()){val f=files.getJSONObject(j);val n=f.optString("name");if(n.endsWith(".mp4",true)&&!n.contains("thumb",true)){chosen=n;break}};if(chosen.isNotBlank())out+=Movie(d.optString("title",id),d.optString("year",""),d.optString("description",""),"https://archive.org/download/$id/"+Uri.encode(chosen))};if(out.size>=12)break};out}
private fun get(u:String):String{val c=URL(u).openConnection() as HttpURLConnection;c.connectTimeout=10000;c.readTimeout=15000;c.setRequestProperty("User-Agent","TheBakersDozen/1.2");return c.inputStream.bufferedReader().use{it.readText()}.also{c.disconnect()}}
@Composable private fun Guide(cs:List<Channel>){Column{Text("EPG / TV Guide",color=Color.White,fontSize=30.sp,fontWeight=FontWeight.Bold);Text("XMLTV-ready guide. Add a lawful EPG source in Settings.",color=Color.Gray);Spacer(Modifier.height(16.dp));cs.forEach{c->Row(Modifier.fillMaxWidth().padding(vertical=6.dp).background(Panel,RoundedCornerShape(8.dp)).padding(16.dp)){Text(c.name,Modifier.width(190.dp),Color.White,fontWeight=FontWeight.Bold);Text("LIVE  Broadcaster schedule / EPG",color=Color.LightGray)}}}}
@Composable private fun VideoAddons(){
 val ctx=androidx.compose.ui.platform.LocalContext.current
 val prefs=remember{ctx.getSharedPreferences("s8_addons",android.content.Context.MODE_PRIVATE)}
 var input by remember{mutableStateOf("")}
 var saved by remember{mutableStateOf(prefs.getStringSet("urls",emptySet())?.toList()?.sorted()?:emptyList())}
 var message by remember{mutableStateOf("Add a compatible repository, catalog, or provider URL.")}
 fun save(){
  val u=input.trim()
  if((u.startsWith("https://")||u.startsWith("http://"))&&u.length>10){
   saved=(saved+u).distinct()
   prefs.edit().putStringSet("urls",saved.toSet()).apply()
   input="";message="Add-on source saved."
  }else message="Enter a valid http:// or https:// URL."
 }
 Column{
  Text("Video Add-ons",color=Color.White,fontSize=30.sp,fontWeight=FontWeight.Bold)
  Text("User-controlled sources",color=Color.Gray)
  Spacer(Modifier.height(14.dp))
  OutlinedTextField(value=input,onValueChange={input=it},label={Text("Repository / add-on URL")},singleLine=true,modifier=Modifier.fillMaxWidth())
  Spacer(Modifier.height(8.dp))
  Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Button({save()}){Text("Add Source")};Text(message,color=Color.LightGray,modifier=Modifier.align(Alignment.CenterVertically))}
  Spacer(Modifier.height(18.dp))
  if(saved.isEmpty()) Text("No user add-on sources saved yet.",color=Color.Gray)
  LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){
   items(saved){u->
    Row(Modifier.fillMaxWidth().background(Panel,RoundedCornerShape(10.dp)).padding(14.dp),verticalAlignment=Alignment.CenterVertically){
     Text(u,color=Color.White,modifier=Modifier.weight(1f),maxLines=2)
     Button({runCatching{ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW,Uri.parse(u)))}}){Text("Open")}
     Spacer(Modifier.width(8.dp))
     TextButton({saved=saved-u;prefs.edit().putStringSet("urls",saved.toSet()).apply()}){Text("Remove")}
    }
   }
  }
 }
}
@Composable private fun Settings(){
 var auto by remember{mutableStateOf(false)}
 if(auto){AutoSetup{auto=false};return}
 Column{
  Text("Settings",color=Color.White,fontSize=30.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(16.dp))
  Card({auto=true},Modifier.fillMaxWidth().padding(vertical=5.dp),colors=CardDefaults.cardColors(containerColor=Accent)){Column(Modifier.padding(18.dp)){Text("Auto Setup",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold);Text("Real-Debrid authorization → Video Add-ons",color=Color.White)}}
  listOf("Playlist / M3U — your lawful streams","EPG / XMLTV — guide-data URL","Movies — Internet Archive public catalog","Video Add-ons — user-controlled repositories","Playback — Media3 HLS / MP4","About — The Baker's Dozen 1.2").forEach{x->Card({},Modifier.fillMaxWidth().padding(vertical=5.dp),colors=CardDefaults.cardColors(containerColor=Panel)){Text(x,color=Color.White,fontSize=18.sp,modifier=Modifier.padding(18.dp))}}
 }
}
@Composable private fun AutoSetup(back:()->Unit){
 val ctx=androidx.compose.ui.platform.LocalContext.current
 var auth by remember{mutableStateOf<RdAuth?>(null)};var status by remember{mutableStateOf("Not connected")}
 Column{
  Row(verticalAlignment=Alignment.CenterVertically){Button(back){Text("Back")};Spacer(Modifier.width(14.dp));Text("Auto Setup",color=Color.White,fontSize=30.sp,fontWeight=FontWeight.Bold)}
  Spacer(Modifier.height(18.dp));Text("1. Real-Debrid",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold);Text(status,color=Color.LightGray);Spacer(Modifier.height(10.dp))
  Button({status="Requesting authorization code…";kotlinx.coroutines.MainScope().launch{runCatching{rdDeviceCode()}.onSuccess{auth=it;status="Open "+it.verificationUrl+" and enter code: "+it.userCode}.onFailure{status="Authorization error: "+(it.message?:"network error")}}}){Text("Authorize Real-Debrid")}
  if(auth!=null){Spacer(Modifier.height(12.dp));Text("Code: "+auth!!.userCode,color=Color.White,fontSize=26.sp,fontWeight=FontWeight.Bold);Button({ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW,Uri.parse(auth!!.verificationUrl)))}){Text("Open Real-Debrid Device Page")}}
  Spacer(Modifier.height(24.dp));Text("2. Video Add-ons",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold);Text("After account authorization, continue to the user-controlled repository/add-on installer.",color=Color.LightGray)
 }
}
private data class RdAuth(val deviceCode:String,val userCode:String,val verificationUrl:String,val interval:Int)
private suspend fun rdDeviceCode():RdAuth=withContext(Dispatchers.IO){val j=JSONObject(get("https://api.real-debrid.com/oauth/v2/device/code?client_id=X245A4XAIBGVM&new_credentials=yes"));RdAuth(j.getString("device_code"),j.getString("user_code"),j.getString("verification_url"),j.optInt("interval",5))}
