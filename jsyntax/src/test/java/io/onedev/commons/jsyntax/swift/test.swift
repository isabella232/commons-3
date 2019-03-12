//
//  ViewController.swift
//  SwiftSideslipLikeQQ
//
//  Created by JohnLui on 15/4/10.
//  Copyright (c) 2015�� com.lvwenhan. All rights reserved.
//

import UIKit

// �� View Controller Ϊ�������������������κ� UI Ԫ��
class ViewController: UIViewController {
    
    // �� TabBar Controller ���Ǵ�ͳ�����ϵ��������ڴ�ֻ�����ṩ UITabBar ��� UI ���
    var mainTabBarController: MainTabBarController!
    
    // �����������ƣ������ڲ˵�����״̬�µ����ҳ���Զ��رղ˵�
    var tapGesture: UITapGestureRecognizer!
    
    // ��ҳ�� Navigation Bar ���ṩ�ߣ�����ҳ������
    var homeNavigationController: UINavigationController!
    // ��ҳ�м����Ҫ��ͼ����Դ
    var homeViewController: HomeViewController!
    // �໬�˵���ͼ����Դ
    var leftViewController: LeftViewController!
    
    // ��������ͼ��ʵ�� UINavigationController.view �� HomeViewController.view һ������
    var mainView: UIView!
    
    // �໬�������
    var distance: CGFloat = 0
    let FullDistance: CGFloat = 0.78
    let Proportion: CGFloat = 0.77
    var centerOfLeftViewAtBeginning: CGPoint!
    var proportionOfLeftView: CGFloat = 1
    var distanceOfLeftView: CGFloat = 50
    
    // �໬�˵���ɫ��͸�����ֲ�
    var blackCover: UIView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // �����������ñ���
        let imageView = UIImageView(image: UIImage(named: "back"))
        imageView.frame = UIScreen.mainScreen().bounds
        self.view.addSubview(imageView)
        
        // ͨ�� StoryBoard ȡ�����໬�˵���ͼ
        leftViewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewControllerWithIdentifier("LeftViewController") as! LeftViewController
        // ���� 4.7 �� 5.5 ����Ļ�����Ų�������ż����С bug
        if Common.screenWidth > 320 {
            proportionOfLeftView = Common.screenWidth / 320
            distanceOfLeftView += (Common.screenWidth - 320) * FullDistance / 2
        }
        leftViewController.view.center = CGPointMake(leftViewController.view.center.x - 50, leftViewController.view.center.y)
        leftViewController.view.transform = CGAffineTransformScale(CGAffineTransformIdentity, 0.8, 0.8)
        
        // ����������ʼ��
        centerOfLeftViewAtBeginning = leftViewController.view.center
        // �Ѳ໬�˵���ͼ���������
        self.view.addSubview(leftViewController.view)
        
        // �ڲ໬�˵�֮�����Ӻ�ɫ���ֲ㣬Ŀ����ʵ���Ӳ���Ч
        blackCover = UIView(frame: CGRectOffset(self.view.frame, 0, 0))
        blackCover.backgroundColor = UIColor.blackColor()
        self.view.addSubview(blackCover)
        
        // ��ʼ������ͼ�������� TabBar��NavigationBar����ҳ������ͼ
        mainView = UIView(frame: self.view.frame)
        // ��ʼ�� TabBar
        let nibContents = NSBundle.mainBundle().loadNibNamed("MainTabBarController", owner: nil, options: nil)
        mainTabBarController = nibContents.first as! MainTabBarController
        // ȡ�� TabBar Controller ����ͼ��������ͼ
        let tabBarView = mainTabBarController.view
        mainView.addSubview(tabBarView)
        // �� StoryBoard ȡ����ҳ�� Navigation Controller
        homeNavigationController = UIStoryboard(name: "Main", bundle: nil).instantiateViewControllerWithIdentifier("HomeNavigationController") as! UINavigationController
        // �� StoryBoard ��ʼ�������� Navigation Controller ���Զ���ʼ������ Root View Controller���� HomeViewController
        // ���ǽ��䣨ָ�룩ȡ������������ View Controller �ĳ�Ա���� homeViewController
        homeViewController = homeNavigationController.viewControllers.first as! HomeViewController
        // �ֱ� Navigation Bar �� homeViewController ����ͼ���� TabBar Controller ����ͼ
        tabBarView.addSubview(homeViewController.navigationController!.view)
        tabBarView.addSubview(homeViewController.view)
        
        // �� TabBar Controller ����ͼ�У��� TabBar ��ͼ�ᵽ����
        tabBarView.bringSubviewToFront(mainTabBarController.tabBar)
        
        // ������ͼ��������
        self.view.addSubview(mainView)
        
        // �ֱ�ָ�� Navigation Bar �������ఴť���¼�
        homeViewController.navigationItem.leftBarButtonItem?.action = Selector("showLeft")
        homeViewController.navigationItem.rightBarButtonItem?.action = Selector("showRight")
        
        // ������ͼ�� UIPanGestureRecognizer
        let panGesture = homeViewController.panGesture
        panGesture.addTarget(self, action: Selector("pan:"))
        mainView.addGestureRecognizer(panGesture)
        
        // ���ɵ�������˵�����
        tapGesture = UITapGestureRecognizer(target: self, action: "showHome")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    // ��Ӧ UIPanGestureRecognizer �¼�
    func pan(recongnizer: UIPanGestureRecognizer) {
        let x = recongnizer.translationInView(self.view).x
        let trueDistance = distance + x // ʵʱ����
        let trueProportion = trueDistance / (Common.screenWidth*FullDistance)
        
        // ��� UIPanGestureRecognizer �������򼤻��Զ�ͣ��
        if recongnizer.state == UIGestureRecognizerState.Ended {

            if trueDistance > Common.screenWidth * (Proportion / 3) {
                showLeft()
            } else if trueDistance < Common.screenWidth * -(Proportion / 3) {
                showRight()
            } else {
                showHome()
            }
            
            return
        }

        // �������ű���
        var proportion: CGFloat = recongnizer.view!.frame.origin.x >= 0 ? -1 : 1
        proportion *= trueDistance / Common.screenWidth
        proportion *= 1 - Proportion
        proportion /= FullDistance + Proportion/2 - 0.5
        proportion += 1
        if proportion <= Proportion { // �������Ѿ��ﵽ��С�����ټ�������
            return
        }
        // ִ���Ӳ���Ч
        blackCover.alpha = (proportion - Proportion) / (1 - Proportion)
        // ִ��ƽ�ƺ����Ŷ���
        recongnizer.view!.center = CGPointMake(self.view.center.x + trueDistance, self.view.center.y)
        recongnizer.view!.transform = CGAffineTransformScale(CGAffineTransformIdentity, proportion, proportion)
        
        // ִ������ͼ����
        let pro = 0.8 + (proportionOfLeftView - 0.8) * trueProportion
        leftViewController.view.center = CGPointMake(centerOfLeftViewAtBeginning.x + distanceOfLeftView * trueProportion, centerOfLeftViewAtBeginning.y - (proportionOfLeftView - 1) * leftViewController.view.frame.height * trueProportion / 2 )
        leftViewController.view.transform = CGAffineTransformScale(CGAffineTransformIdentity, pro, pro)
    }
    
    // ��װ�������������ں��ڵ���
    
    // չʾ����ͼ
    func showLeft() {
        // ����ҳ ���� ����Զ��رղ໬�˵�����
        mainView.addGestureRecognizer(tapGesture)
        // ������룬ִ�в˵��Զ���������
        distance = self.view.center.x * (FullDistance*2 + Proportion - 1)
        doTheAnimate(self.Proportion, showWhat: "left")
        homeNavigationController.popToRootViewControllerAnimated(true)
    }
    // չʾ����ͼ
    func showHome() {
        // ����ҳ ɾ�� ����Զ��رղ໬�˵�����
        mainView.removeGestureRecognizer(tapGesture)
        // ������룬ִ�в˵��Զ���������
        distance = 0
        doTheAnimate(1, showWhat: "home")
    }
    // չʾ����ͼ
    func showRight() {
        // ����ҳ ���� ����Զ��رղ໬�˵�����
        mainView.addGestureRecognizer(tapGesture)
        // ������룬ִ�в˵��Զ���������
        distance = self.view.center.x * -(FullDistance*2 + Proportion - 1)
        doTheAnimate(self.Proportion, showWhat: "right")
    }
    // ִ�����ֶ�������ʾ���˵�����ʾ��ҳ����ʾ�Ҳ�˵�
    func doTheAnimate(proportion: CGFloat, showWhat: String) {
        UIView.animateWithDuration(0.3, delay: 0, options: UIViewAnimationOptions.CurveEaseInOut, animations: { () -> Void in
            // �ƶ���ҳ����
            self.mainView.center = CGPointMake(self.view.center.x + self.distance, self.view.center.y)
            // ������ҳ
            self.mainView.transform = CGAffineTransformScale(CGAffineTransformIdentity, proportion, proportion)
            if showWhat == "left" {
                // �ƶ����˵�������
                self.leftViewController.view.center = CGPointMake(self.centerOfLeftViewAtBeginning.x + self.distanceOfLeftView, self.leftViewController.view.center.y)
                // �������˵�
                self.leftViewController.view.transform = CGAffineTransformScale(CGAffineTransformIdentity, self.proportionOfLeftView, self.proportionOfLeftView)
            }
            // �ı��ɫ���ֲ��͸���ȣ�ʵ���Ӳ�Ч��
            self.blackCover.alpha = showWhat == "home" ? 1 : 0

            // Ϊ����ʾЧ�������Ҳ�˵�����ʱ����©�������˵�������ʵ������
            self.leftViewController.view.alpha = showWhat == "right" ? 0 : 1
            }, completion: nil)
    }

}

