import {
  AfterViewInit,
  Component,
  ElementRef,
  HostListener,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import * as joint from "jointjs";
import {dia} from "jointjs";
import Paper = dia.Paper;
import {StateMachineItems} from "./state-machine-items";
import {ScenarioGroup} from "../../model/scenario-group";
import Element = dia.Element;
import {UtilService} from "../../services/util.service";
import {StateLink} from "../../model/state-link";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {SelfLink} from "../../model/self-link";
import LinkView = dia.LinkView;


@Component({
  selector: 'wm-state-machine',
  templateUrl: './state-machine.component.html',
  styleUrls: ['./state-machine.component.scss']
})
export class StateMachineComponent implements OnInit, OnChanges, AfterViewInit {

  private static readonly ANY = '{{ANY}}';

  @Input()
  item: ScenarioGroup;

  @ViewChild('canvas')
  canvas: ElementRef;

  @Input()
  grid: boolean = true;

  private graph = new joint.dia.Graph();
  private paper: Paper;

  private states: Map<String, Element>;
  private links: StateLink[];

  private dragStartPosition = null;

  constructor(private container: ElementRef, private modalService: NgbModal) {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (UtilService.isUndefined(this.item) || UtilService.isUndefined(this.item.mappings)) {
      return;
    }

    this.graph.clear();

    this.initPaper();

    this.states = new Map<String, Element>();
    this.links = [];

    this.searchForStates(this.states);
    this.addLinks(this.links, this.states);
    this.addStatesToGraph(this.states);
    this.addLinksToGraph(this.links, this.states);
    this.doLayout();

    this.selfLinks(this.links);
    this.sameDirectionLinks(this.links);
  }

  private initPaper() {
    this.paper = new joint.dia.Paper({
      el: this.canvas.nativeElement,
      model: this.graph,
      height: this.container.nativeElement.offsetHeight,
      width: this.container.nativeElement.offsetWidth,
      gridSize: 10
    });
    this.paper.translate(0, 0);

    this.dragStartPosition = null;

    this.paper.on('blank:pointerdown', (event, x, y) => {
      this.dragStartPosition = {x: x, y: y}
    });

    this.paper.on('cell:pointerup blank:pointerup', (cellView, x, y) => {
      this.dragStartPosition = null;
    });
  }

  private searchForStates(states: Map<String, dia.Element>) {
    this.item.stateNames.forEach(stateName => {
      if (stateName === 'Started') {
        states.set(stateName, StateMachineItems.createStartState());
      } else {
        states.set(stateName, StateMachineItems.createState(stateName));
      }
    });
  }

  private addLinks(links: StateLink[], states: Map<String, dia.Element>) {
    this.item.mappings.forEach(mapping => {
      if (UtilService.isDefined(mapping.requiredScenarioState) && UtilService.isDefined(mapping.newScenarioState)) {
        // A -> B
        links.push(new StateLink(mapping.requiredScenarioState, mapping.newScenarioState, mapping));
      } else if (UtilService.isDefined(mapping.newScenarioState)) {
        // any -> B
        if (UtilService.isUndefined(states.get(StateMachineComponent.ANY))) {
          states.set(StateMachineComponent.ANY, StateMachineItems.createAnyState());
        }
        links.push(new StateLink(StateMachineComponent.ANY, mapping.newScenarioState, mapping));
      } else if (UtilService.isDefined(mapping.requiredScenarioState)) {
        // A -> A
        links.push(new StateLink(mapping.requiredScenarioState, mapping.requiredScenarioState, mapping));
      }
    });
  }

  private addStatesToGraph(states: Map<String, dia.Element>) {
    states.forEach((state, stateName) => {
      // state.position(pos.x * widthFactor + this.container.nativeElement.offsetWidth / 2, pos.y * heightFactor + this.container.nativeElement.offsetHeight / 2);
      state.addTo(this.graph);
    });
  }

  private addLinksToGraph(links: StateLink[], states: Map<String, dia.Element>) {
    links.forEach(data => {
      const link = new joint.shapes.standard.Link();
      link.source(states.get(data.source));
      link.target(states.get(data.target));

      if (data.source === data.target) {
        link.connector('rounded', {
          radius: 20
        });
        link.router('manhattan', {
          step: 10,
          padding: 15,
          maxAllowedDirectionChange: 0
        });
      } else {
        link.connector('rounded');
        link.router('normal', {
          step: 30,
          padding: 30
        });
      }

      data.link = link;

      link.addTo(this.graph);
      const linkView = link.findView(this.paper);
      linkView.addTools(StateMachineItems.createInfoButton(this.modalService, data.mapping));
    });

  }

  private doLayout() {
    joint.layout.DirectedGraph.layout(this.graph, {
      nodeSep: 100,
      edgeSep: 100,
      rankSep: 100,
      clusterPadding: 50,
      rankDir: "TB",
      marginX: 100,
      marginY: 50
    });
  }

  ngAfterViewInit(): void {
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    if (UtilService.isDefined(this.paper)) {
      this.paper.setDimensions(0, 0);
      this.paper.setDimensions(this.container.nativeElement.offsetWidth, this.container.nativeElement.offsetHeight);
    }
  }

  onMove(event: MouseEvent) {
    if (UtilService.isDefined(this.paper) && UtilService.isDefined(this.dragStartPosition)) {
      this.paper.translate(event.offsetX - this.dragStartPosition.x, event.offsetY - this.dragStartPosition.y);
      //  var scale = V(paper.viewport).scale();
      // dragStartPosition = { x: x * scale.sx, y: y * scale.sy};
    }
  }

  private selfLinks(links: StateLink[]) {

    const gap = 20;
    const linkMap = new Map<String, StateLink[]>();

    links.forEach(data => {
      if (data.target === data.source) {
        if (UtilService.isUndefined(linkMap.get(data.source))) {
          linkMap.set(data.source, []);
        }
        linkMap.get(data.source).push(data);
      }
    });

    linkMap.forEach(selfLinks => {
      selfLinks.forEach((data, index) => {
        const linkView = this.paper.findViewByModel(data.link.id) as LinkView;
        const conn = linkView.getConnection();

        // TODO: remove verticies and set again. Needed if this should work with moving elements
        // const vertices = data.link.vertices();
        //
        // vertices.forEach(value =>{
        //   data.link.removeVertex(0);
        // });

        linkView.addVertex(conn.bbox().center().x - gap * (index + 1), conn.bbox().center().y - gap/2 * (index + 1));
        linkView.addVertex(conn.bbox().center().x - gap/2 * (index + 1), conn.bbox().center().y - gap * (index + 1));
      });
    });
  }

  private sameDirectionLinks(links: StateLink[]) {
    const gap = 20;
    const linkMap = new Map<String, StateLink[]>();

    links.forEach(data => {
      if (data.target !== data.source) {
        if (UtilService.isUndefined(linkMap.get(data.source + data.target))) {
          linkMap.set(data.source + data.target, []);
        }
        linkMap.get(data.source + data.target).push(data);
      }
    });

    linkMap.forEach(selfLinks => {
      selfLinks.forEach((data, index) => {
        const linkView = this.paper.findViewByModel(data.link.id) as LinkView;
        const conn = linkView.getConnection();

        const center = linkView.getConnection().bbox().center();

        // TODO: remove verticies and set again. Needed if this should work with moving elements
        // const vertices = data.link.vertices();
        //
        // vertices.forEach(value =>{
        //   data.link.removeVertex(0);
        // });

        linkView.addVertex(center.x - gap * index, center.y);
      });
    });
  }
}
