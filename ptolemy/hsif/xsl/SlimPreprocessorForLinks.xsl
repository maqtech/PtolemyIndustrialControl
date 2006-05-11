<?xml version="1.0"?>
<!-- 	
 Copyright (c) 2003-2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
                                        
@ProposedRating Red (hyzheng)
@AcceptedRating Red (cxh)
	
This file strips away the redundant links.

@author Haiyang Zheng
@version $Id$ 
@since HyVisual 2.2
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                         xmlns:xalan="http://xml.apache.org/xslt" version="2.0">

	<!-- DOCTYPE element includes public ID and system ID -->
	<!--xsl:output method="xml" indent="yes" omit-xml-declaration="no"/-->
	<xsl:output method="xml" doctype-system="http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd"
		    doctype-public="-//UC Berkeley//DTD MoML 1//EN"/>

    <!-- time function -->
    <xsl:variable name="now" xmlns:Date="/java.util.Date">
        <xsl:value-of select="Date:toString(Date:new())"/>
    </xsl:variable>

    <!-- configuration -->
    <xsl:param name="author">Ptolemy II</xsl:param>
    <xsl:preserve-space elements="*"/>

    <!-- ==========================================================
          root element
          ========================================================== -->
    <xsl:template match="/">
     <xsl:comment>
		Generated by <xsl:value-of select="$author"/> at <xsl:value-of select="$now"/>.
	 </xsl:comment>
     <xsl:apply-templates/>
    </xsl:template>

    <!-- General-Copy which copies everything. -->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
            </xsl:for-each>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- This template strips away redundant links. -->
    <xsl:template match="link">
        <xsl:variable name="port" select="@port"/>
        <xsl:variable name="relation" select="@relation"/>
        <xsl:choose>
            <xsl:when test="not(preceding-sibling::link[@port=$port and @relation=$relation])">
                <xsl:copy>
                    <xsl:for-each select="@*">
                        <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
                    </xsl:for-each>
                    <xsl:apply-templates/>
                </xsl:copy>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
