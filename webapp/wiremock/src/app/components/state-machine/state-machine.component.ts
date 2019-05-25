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

    const states: Map<String, Element> = new Map<String, Element>();
    const links: StateLink[] = [];

    this.searchForStates(states);
    this.addLinks(links, states);
    this.addStatesToGraph(states);
    this.addLinksToGraph(links, states);
    this.doLayout();



    this.paper.on('blank:pointerdown', (event, x, y) => {
      this.dragStartPosition = {x: x, y: y}
    });

    this.paper.on('cell:pointerup blank:pointerup', (cellView, x, y) => {
      this.dragStartPosition = null;
    });
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
      link.router('manhattan', {
        step: 30,
        padding: 30
      });
      link.connector('smooth');
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
    this.paper.setDimensions(0, 0);
    this.paper.setDimensions(this.container.nativeElement.offsetWidth, this.container.nativeElement.offsetHeight);
  }

  onMove(event: MouseEvent) {
    if (UtilService.isDefined(this.dragStartPosition)) {
      this.paper.translate(event.offsetX - this.dragStartPosition.x, event.offsetY - this.dragStartPosition.y);
      //  var scale = V(paper.viewport).scale();
      // dragStartPosition = { x: x * scale.sx, y: y * scale.sy};
    }
  }
}
