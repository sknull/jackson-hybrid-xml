package de.visualdigits.hybridxml.model.html

import org.junit.jupiter.api.Test

class HtmlTest {

    @Test
    fun testReadModel() {
        val htmlRaw = """<p>Welche Firmen genau zum Wirtschaftsverband der Familienunternehmen gehören, ist nicht öffentlich bekannt. Was hingegen bekannt ist, ist dass die in Burgwedel bei Hannover ansässige <a>Drogeriekette Rossmann nun kein Mitglied mehr</a> ist.</p>
<p>Das Unternehmen hat den Verband verlassen, weil zu einem Parlamentarischen Abend in Berlin im Oktober auch Vertreter der in Teilen rechtsextremen AfD eingeladen wurden. Die Familienunternehmer hatten zuvor angekündigt, der Verband wolle sich für Gespräche mit der AfD öffnen. Ein "Kontaktverbot" zu AfD-Bundestagsabgeordneten sei aufgehoben worden, so Verbandspräsidentin Marie-Christine Ostermann. Die bisherige Strategie der Brandmauer zur AfD - sie ist damit aufgehoben. "Wir unterstützen die Haltung des Verbands Die Familienunternehmer nicht und haben die Mitgliedschaft gekündigt", bestätigt eine Sprecherin von Rossmann. Weiter wolle man sich jedoch nicht dazu äußern.</p>
<h2>Auch Fritz Kola verlässt Verband</h2>
<p>Am Donnerstagnachmittag teilt auch der Hamburger Limonade-Hersteller Fritz Kola mit, sich künftig anderweitig vernetzen und als wirtschaftlicher Akteur präsent zu sein zu wollen. Das Unternehmen begründete das gegenüber der Nachrichtenagentur dpa: "Eine offene, demokratische Gesellschaft bildet für uns die Grundlage wirtschaftlichen und gesellschaftlichen Handelns. Vor diesem Hintergrund haben wir unsere Mitgliedschaft im Verband beendet". Fritz Kola werde neue Wege suchen, um weiterhin mit anderen Unternehmerinnen und Unternehmern im Austausch zu bleiben.</p>
<h2>Neun von zehn angefragten Unternehmen äußern sich nicht</h2>
<p>Der NDR hat zehn größere norddeutsche Familienunternehmen angefragt - keines wollte ein Interview geben. Und nur eines, nämlich das Medizintechnik-Unternehmen Drägerwerk mit Sitz in Lübeck, schickt eine Stellungnahme des Vorstandsvorsitzenden Stefan Dräger. Er stellt sich darin hinter den Verband, in dem sein Unternehmen Mitglied ist und argumentiert, es sei immer richtig, miteinander zu reden statt übereinander.</p>
<p>Das gelte in allen Diskussionen, die die Gesellschaft spalten. Er sagt: "Auch wenn es anstrengend ist: Wir müssen die AfD in der Sache widerlegen und aufzeigen, dass die demokratiefeindlichen und wirtschaftsfeindlichen Positionen den Wohlstand gefährden."</p>
<h2>Schulte-Südhof: Keine Sorgen vor Öffnung</h2>
<p>Auch André Schulte-Südhof, der den Regionalverband der Familienunternehmer in Niedersachsen leitet, sieht das so. Dem NDR sagte er, er wisse, dass "unsere Präsidentin den richtigen Kompass hat". Deswegen würde er sich wenig Sorgen machen, "dass wir uns dieser Partei öffnen, wir wollen uns einfach inhaltlich mit den Themen auseinandersetzen."</p>
<p>Schult-Südhof führt ein Unternehmen für Absaug- und Filtertechnik im Landkreis Osnabrück. Der Großteil seiner Aufträge komme aus dem europäischen Ausland - daher bereiten ihm die aktuellen Umfragewerte der AfD große Sorgen.</p>
<h2>Keine abgestimmte Haltung in Niedersachsen</h2>
<p>"Insbesondere das Thema Europapolitik - wenn es da Bestrebungen geben sollte, auszutreten, wäre das massiv schädigend für unsere Familienunternehmen auch in Deutschland", so der Unternehmer. Die Entscheidung des Bundes-Verbands werde Thema beim nächsten Treffen der Landesvorsitzenden sein. Noch gebe es in Niedersachsen keine abgestimmte Haltung.</p>
<p>Der Verband der Familienunternehmer reagierte nicht auf eine NDR Anfrage. Auf seiner Website betont er: Eine Regierungsbeteiligung der AfD lehne man ab, eine inhaltliche Auseinandersetzung scheue man aber nicht. Mit Andersdenkenden zu diskutieren, heiße nicht, deren Positionen zu akzeptieren.</p>
<h2>Wirtschaftlicher Schaden befürchtet</h2>
<p>Marcel Fratzscher, Präsident des Deutschen Instituts für Wirtschaftsforschung, sieht genau das kritisch und sagt, es sei ein fatales Signal an Unternehmen, Investoren, an Arbeitgeberinnen, Arbeitgeber im Inland wie im Ausland. Denn das würde bedeuten, man arrangiere sich mit der AfD. Er sagt: "Wohlwissend, wofür die AfD steht - die enormen wirtschaftlichen Schaden in Deutschland anrichten würde."</p>
<p>Die Landesverbände in Schleswig-Holstein, Niedersachsen und Hamburg sagen auf Anfrage, dass in ihren Bundesländern bisher keine Gespräche mit der AfD geführt wurden - der Landesverband in Mecklenburg-Vorpommern war nicht erreichbar. Dort ist die AfD nach aktuellen Umfragen stärkste politische Kraft - in weniger als einem Jahr wird ein neuer Landtag gewählt.</p></div>
"""
        val html = Html.parseHtml(htmlRaw)
        println(html?.writeXmlValue(indentOutput = true, writeXmlDeclaration = false))
    }
}