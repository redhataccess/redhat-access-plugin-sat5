%define version 0.3.17

Name:	redhat-access-plugin-sat5
Version:	%{version}
Release:	1%{?dist}
Summary:	Red Hat Access Plugin for Satellite 5.7
Group:	Applications/System	
License:	MIT
Source0: redhat-access-plugin-sat5.tar.gz 
BuildArch: noarch
#BuildRequires:	
#Requires:	

%description
Red Hat Access Plugin for Satellite 5.7


%prep
echo "------------------------------------------- PREP -------------------------------------------"
echo "------------------------------------------- PREP -------------------------------------------"
echo "------------------------------------------- PREP -------------------------------------------"
echo "------------------------------------------- PREP -------------------------------------------"
echo "------------------------------------------- PREP -------------------------------------------"


%build
echo "------------------------------------------- BUILD -------------------------------------------"
echo "------------------------------------------- BUILD -------------------------------------------"
echo "------------------------------------------- BUILD -------------------------------------------"
echo "------------------------------------------- BUILD -------------------------------------------"
echo "------------------------------------------- BUILD -------------------------------------------"


%install
echo "------------------------------------------- INSTALL -------------------------------------------"
echo "------------------------------------------- INSTALL -------------------------------------------"
echo "------------------------------------------- INSTALL -------------------------------------------"
echo "------------------------------------------- INSTALL -------------------------------------------"
echo "------------------------------------------- INSTALL -------------------------------------------"



%changelog
* Tue Jun 3 2015 Chris Kyrouac <ckyrouac@redhat.com>
- Enable linking to specific insights tab

* Mon Jun 2 2015 Chris Kyrouac <ckyrouac@redhat.com>
- Fix Systems->Insights display logic
- Add product info to branch_info endpoint
- Add rules management to GUI
- Add more logging statements to proxy

* Mon Jun 1 2015 Chris Kyrouac <ckyrouac@redhat.com>
- Proxy to r/insights
- Add Systems-Insights-Setup tab
- Bug fixes

* Fri May 22 2015 Chris Kyrouac <ckyrouac@redhat.com>
- Fall back to executing hostname to retrieve branchid when InetAddress fails

* Thu May 21 2015 Chris Kyrouac <ckyrouac@redhat.com>
- Encrypt stored password

* Wed May 20 2015 Chris Kyrouac <ckyrouac@redhat.com>
- More bug fixes

* Mon May 18 2015 Chris Kyrouac <ckyrouac@redhat.com>
- Various bug fixes

* Mon May 18 2015 Chris Kyrouac <ckyrouac@redhat.com>
- Sign the RPM, update client RPM name

* Thu May 14 2015 Chris Kyrouac <ckyrouac@redhat.com>
- Logging, bug fixes

* Thu May 14 2015 Chris Kyrouac <ckyrouac@redhat.com>
- Testing tomcat cache

* Thu May 14 2015 Chris Kyrouac <ckyrouac@redhat.com>
- Remove config stuff

* Wed May 13 2015 Chris Kyrouac <ckyrouac@redhat.com>
- Tech preview mostly ready

* Thu Feb 12 2015 Chris Kyrouac <ckyrouac@redhat.com>
- Version 1

