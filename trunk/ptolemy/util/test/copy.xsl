<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml"/>	
    <xsl:template match="*|@*">
	<xsl:copy>
	    <xsl:apply-templates select="@*"/>
	    <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>