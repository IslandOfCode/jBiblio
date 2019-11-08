<!DOCTYPE html>
<html>
	<head>
		<style>
			#logo {
			}
			#logo img {
				width: 128px;
				vertical-align: bottom;
			}
			#logo span.title {
				font-size: 200%;
				font-family: arial;
			}
			#logo span.subtitle {
				font-style: italic;
				font-size: 120%;
			}
			table.cinereo {
				border: 1px solid #666666;
				width: 100%;
				text-align: center;
				border-collapse: collapse;
			}
			table.cinereo td, table.cinereo th {
				border: 1px solid #948473;
				padding: 5px 0px;
			}
			table.cinereo tbody td {
				font-size: 14px;
			}
			table.boldest tbody td {
				font-size: 22px;
				font-weight: bold;
			}
			table.topten tbody td {
				text-align:left !important;
			}
			
			table.cinereo thead {
				background: #666666;
			}
			table.cinereo thead th {
				font-size: 16px;
				font-weight: bold;
				color: #EEEEEE;
				text-align: center;
			}
			table.cinereo tfoot td {
				font-size: 16px;
			}
		</style>
	</head>
	<body>
		<div id="logo">
			<img src=" data:image/png;base64,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" alt="LOGO" />
			<span class="title">BIBLIOTECA SCOLASTICA</span><br/><br/>
			<span class="subtitle">${titoloOwner}: ${nomeOwner!""}</span>
		</div>
		<br/>
		<hr style="width:95%"/>
		
		<p style="text-align:center; font-size: 16pt; font-weight: bold; font-style:italic">La biblioteca in cifre</p>
		<table class="cinereo boldest" style="width:70% !important; margin:auto">
			<thead>
				<th>Totale libri</th>
				<th>Totale clienti</th>
				<th>Totale prestiti</th>
				<th>Totale libri prestati</th>
			</thead>
			<tbody>
				<td>${totLibri}</td>
				<td>${totClienti}</td>
				<td>${totPrestiti}</td>
				<td>${totLibriPrestati}</td>
			</tbody>
		</table>
		<br/>
		<table class="cinereo boldest" style="width:50% !important; margin:auto">
			<thead>
				<th>Libri presenti</th>
				<th>Clienti attivi</th>
				<th>Prestiti risolti</th>
			</thead>
			<tbody>
				<td>${totLibriPresenti}</td>
				<td>${totClientiAttivi}</td>
				<td>${totPrestitiRisolti}</td>
			</tbody>
		</table>

		<p style="text-align:center; font-size: 16pt; font-weight: bold; font-style:italic">Prestiti per mese (${AS2})</p>
		
		<table class="cinereo boldest" style="width:85% !important; margin:auto">
			<thead>
				<th>GEN</th>
				<th>FEB</th>
				<th>MAR</th>
				<th>APR</th>
				<th>MAG</th>
				<th>GIU</th>
				<th>LUG</th>
				<th>AGO</th>
				<th>SET</th>
				<th>OTT</th>
				<th>NOV</th>
				<th>DIC</th>
			</thead>
			<tbody>
				<td>${prestitiGEN}</td>
				<td>${prestitiFEB}</td>
				<td>${prestitiMAR}</td>
				<td>${prestitiAPR}</td>
				<td>${prestitiMAG}</td>
				<td>${prestitiGIU}</td>
				<td>${prestitiLUG}</td>
				<td>${prestitiAGO}</td>
				<td>${prestitiSET}</td>
				<td>${prestitiOTT}</td>
				<td>${prestitiNOV}</td>
				<td>${prestitiDIC}</td>
			</tbody>
		</table>
		<p style="text-align:center"><b style="font-size: 14pt">*</b>: <small>il conteggio del mese corrente potrebbe essere solo parziale e soggetto a cambiamenti.</small></p>
		<p style="font-size: 14pt; text-align:center">In totale sono stati prestati <span style="font-size: 18pt; font-weight:bold">${totPrestitiAnnoCorrente}</span> libri quest'anno.</p>
		
		<p style="text-align:center; font-size: 16pt; font-weight: bold; font-style:italic">TOP 10</p>
		<table class="cinereo topten" style="width:90% !important; margin:auto">
			<thead>
				<th>Top 10<br/>libri pi&ugrave; richiesti</th>
				<th>Top 10<br/>classi pi&ugrave; attive</th>
			</thead>
			<tbody>
				<td style="width:70%">
					<ol>
						<#list topLibri as libro>
							<li>${libro}</li>
						</#list>
					</ol>
				</td>
				<td style="width:30%">
					<ol>
						<#list topClassi as classe>
							<li>${classe}</li>
						</#list>
					</ol>
				</td>
			</tbody>
		</table>
		
		<p style="text-align:center; font-size: 16pt; font-weight: bold; font-style:italic">Statistiche utenza</p>
		
		<ul style="margin-left: 5%">
			<li>Giorni di prestito medio: <b>${avgDurataPrestito}</b> (<small><i>Per i prestiti in ritardo si contano solo i giorni di prestito concessi</i></small>).</li>
			<li>Media giorni di ritardo: <b>${avgRitardo}</b> giorni (<small><i>Calcolato sui soli prestiti in ritardo!</i></small>).</li>
			<li>Prestiti in ritardo: <b>${totPrestitiRitardo}</b> su ${totPrestitiAnnoCorrente} totali.</li>
		</ul>
		
	</body>
</html>