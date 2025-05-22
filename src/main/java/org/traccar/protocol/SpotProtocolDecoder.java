/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
/*     */ import io.netty.channel.Channel;
/*     */ import io.netty.handler.codec.http.FullHttpRequest;
/*     */ import io.netty.handler.codec.http.HttpResponseStatus;
/*     */ import java.io.InputStream;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import javax.xml.parsers.DocumentBuilder;
/*     */ import javax.xml.parsers.DocumentBuilderFactory;
/*     */ import javax.xml.parsers.ParserConfigurationException;
/*     */ import javax.xml.xpath.XPath;
/*     */ import javax.xml.xpath.XPathConstants;
/*     */ import javax.xml.xpath.XPathExpression;
/*     */ import javax.xml.xpath.XPathFactory;
/*     */ import org.traccar.BaseHttpProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DateUtil;
/*     */ import org.traccar.model.Position;
/*     */ import org.w3c.dom.Document;
/*     */ import org.w3c.dom.Node;
/*     */ import org.w3c.dom.NodeList;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class SpotProtocolDecoder
/*     */   extends BaseHttpProtocolDecoder
/*     */ {
/*     */   private DocumentBuilder documentBuilder;
/*     */   private XPath xPath;
/*     */   private XPathExpression messageExpression;
/*     */   
/*     */   public SpotProtocolDecoder(Protocol protocol) {
/*  50 */     super(protocol);
/*     */     try {
/*  52 */       DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
/*  53 */       builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
/*  54 */       builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
/*  55 */       builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
/*  56 */       builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
/*  57 */       builderFactory.setXIncludeAware(false);
/*  58 */       builderFactory.setExpandEntityReferences(false);
/*  59 */       this.documentBuilder = builderFactory.newDocumentBuilder();
/*  60 */       this.xPath = XPathFactory.newInstance().newXPath();
/*  61 */       this.messageExpression = this.xPath.compile("//messageList/message");
/*  62 */     } catch (ParserConfigurationException|javax.xml.xpath.XPathExpressionException e) {
/*  63 */       throw new RuntimeException(e);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  71 */     FullHttpRequest request = (FullHttpRequest)msg;
/*     */     
/*  73 */     Document document = this.documentBuilder.parse((InputStream)new ByteBufferBackedInputStream(request.content().nioBuffer()));
/*  74 */     NodeList nodes = (NodeList)this.messageExpression.evaluate(document, XPathConstants.NODESET);
/*     */     
/*  76 */     List<Position> positions = new LinkedList<>();
/*     */     
/*  78 */     for (int i = 0; i < nodes.getLength(); i++) {
/*  79 */       Node node = nodes.item(i);
/*  80 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { this.xPath.evaluate("esnName", node) });
/*  81 */       if (deviceSession != null) {
/*     */         
/*  83 */         Position position = new Position(getProtocolName());
/*  84 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/*  86 */         position.setValid(true);
/*  87 */         position.setTime(DateUtil.parseDate(this.xPath.evaluate("timestamp", node)));
/*  88 */         position.setLatitude(Double.parseDouble(this.xPath.evaluate("latitude", node)));
/*  89 */         position.setLongitude(Double.parseDouble(this.xPath.evaluate("longitude", node)));
/*     */         
/*  91 */         String event = this.xPath.evaluate("messageType", node);
/*  92 */         if (event != null && !event.isEmpty()) {
/*  93 */           position.set("event", event);
/*     */         } else {
/*  95 */           position.set("event", "POWER-CHANGE");
/*     */         } 
/*     */         
/*  98 */         positions.add(position);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 103 */     sendResponse(channel, HttpResponseStatus.OK);
/* 104 */     return positions;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SpotProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */