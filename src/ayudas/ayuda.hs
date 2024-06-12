<?xml version="1.0" encoding="ISO-8859-1"?>
<helpset>
    <title>Ayuda TFG</title>
    <maps>
        <homeID>index</homeID>		
        <mapref location="ayuda.jhm"/>	
    </maps>
        
    <view>						
        <name>toc</name>
        <label>Tabla contenidos</label>	
        <type>javax.help.TOCView</type>
        <data>ayudaTOC.xml</data>		
    </view>        
        
    <view>						
        <name>indice</name>
        <label>�ndice</label>	
        <type>javax.help.IndexView</type>
        <data>ayudaIndice.xml</data>		
    </view>                
        
    <view>						
        <name>favoritos</name>
        <label>Favoritos</label>	
        <type>javax.help.FavoritesView</type>
    </view>                        
        
    <view>						
        <name>busqueda</name>
        <label>B�squeda</label>	
        <type>javax.help.SearchView</type>
        <data engine="com.sun.java.help.search.DefaultSearchEngine">JavaHelpSearch</data>
    </view>                        
        
    <presentation default="true" displayviews="false" displayviewimages="true">
        <name>MainWin</name>
        <size width="750" height="850"/>		
        <location x="300" y="200"/>			
        <title></title> 
        <toolbar>	
            <helpaction image="BackwardIco">javax.help.BackAction</helpaction>
            <helpaction image="ForwardIco">javax.help.ForwardAction</helpaction>
            <helpaction image="imgAnhadirFavorito">javax.help.FavoritesAction</helpaction>                        
        </toolbar>
    </presentation>                



</helpset>
