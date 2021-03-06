import {Component} from '@angular/core';
import {PageComponent} from "../../../common/page/page-component";
import {BreadcrumbElement, BreadcrumbService} from "../../../common/breadcrumb.service";
import {ActivatedRoute} from "@angular/router";
import {Title} from "@angular/platform-browser";
import {ColumnDefinition} from "../../../common/generic-table/column-definition";
import {LogbookOperationComponent} from "../../../admin/logbook-operation/logbook-operation.component";
import {LogbookService} from "../../logbook.service";
import {ArchiveUnitHelper} from "../../../archive-unit/archive-unit.helper";
import {VitamResponse} from "../../../common/utils/response";

const breadcrumb: BreadcrumbElement[] = [
  {label: 'Entrée', routerLink: ''},
  {label: 'Suivi des opérations d\'entrée', routerLink: 'ingest/logbook'},
  {label: 'Détail d\'une opération d\'entrée', routerLink: ''}
];

@Component({
  selector: 'vitam-logbook-details',
  templateUrl: './logbook-details.component.html',
  styleUrls: ['./logbook-details.component.css']
})
export class LogbookDetailsComponent extends PageComponent {
  id: string;
  public response: VitamResponse;

  public columns = [
    ColumnDefinition.makeStaticColumn('evTypeProc', 'Catégorie d\'opération', undefined,
      () => ({'width': '175px', 'overflow-wrap': 'break-word'})),
    ColumnDefinition.makeStaticColumn('evType', 'Opération', undefined,
      () => ({'width': '175px', 'overflow-wrap': 'break-word'})),
    ColumnDefinition.makeStaticColumn('rightsStatementIdentifier', 'Contrat associé', undefined,
      () => ({'width': '175px', 'overflow-wrap': 'break-word'})),
    ColumnDefinition.makeStaticColumn('evDateTime', 'Date de début',
      this.archiveUnitHelper.handleDateWithTime, () => ({'width': '125px', 'overflow-wrap': 'break-word'})),
    ColumnDefinition.makeSpecialValueColumn('Date de fin', (item) => item.events[item.events.length - 1].evDateTime,
      this.archiveUnitHelper.handleDateWithTime, () => ({'width': '125px', 'overflow-wrap': 'break-word'})),
    ColumnDefinition.makeSpecialValueColumn('Statut', (item) => item.events[item.events.length - 1].outcome,
      undefined, () => ({'width': '125px', 'overflow-wrap': 'break-word'})),
    ColumnDefinition.makeSpecialValueColumn('Message', (item) => item.events[item.events.length - 1].outMessg,
      undefined, () => ({'width': '125px', 'overflow-wrap': 'break-word'})),
    ColumnDefinition.makeStaticColumn('agIdExt', 'Acteur de l\'opération', undefined,
      () => ({'width': '175px', 'overflow-wrap': 'break-word'}))
  ];

  public extraColumns = [
    ColumnDefinition.makeStaticColumn('evId', 'Identifiant de l\'opération', undefined,
      () => ({'width': '175px', 'overflow-wrap': 'break-word'})),
    ColumnDefinition.makeStaticColumn('evDetData', 'Informations sur l\'opération', undefined,
      () => ({'width': '175px', 'overflow-wrap': 'break-word'})),
    ColumnDefinition.makeStaticColumn('agId', 'Acteur(s) internes', undefined,
      () => ({'width': '175px', 'overflow-wrap': 'break-word'})),
    ColumnDefinition.makeStaticColumn('agIdApp', 'Identifiant de l\'application demandée', undefined,
      () => ({'width': '175px', 'overflow-wrap': 'break-word'})),
    ColumnDefinition.makeStaticColumn('evIdReq', 'Numéro de transaction', undefined,
      () => ({'width': '175px', 'overflow-wrap': 'break-word'})),
    ColumnDefinition.makeStaticColumn('obId', 'Identifiant de l\'opération', undefined,
      () => ({'width': '175px', 'overflow-wrap': 'break-word'})),
    ColumnDefinition.makeSpecialIconColumn('Rapport', (item) => item.evTypeProc.toUpperCase() === 'INGEST' ? ['fa-download'] : [],
      () => ({
        'width': '75px',
        'overflow-wrap': 'break-word'
      }), LogbookOperationComponent.downloadReports, this.logbookService)
  ];

  constructor(private route: ActivatedRoute, public logbookService: LogbookService,
              public titleService: Title, public breadcrumbService: BreadcrumbService,
              public archiveUnitHelper: ArchiveUnitHelper) {
    super('Détail d\'une opération d\'entrée', breadcrumb, titleService, breadcrumbService);
  }

  pageOnInit() {
    this.route.params.subscribe(params => {
      this.id = params['id'];
      this.breadcrumb[this.breadcrumb.length - 1] = {
        label: 'Détail d\'une opération d\'entrée ' + this.id,
        routerLink: 'ingest/logbookOperation/' + this.id
      };
      this.breadcrumbService.changeState(this.breadcrumb);
      this.logbookService.getDetails(this.id).subscribe(
        (data) => {
          this.response = data;
        }
      )
    });
  }

}
